// src/graph.js
import {downloadDrawioFile} from './drawioSave.js';
import {HEAD_LINE_HEIGHT, NODE_WIDTH, PORT_LINE_HEIGHT} from './constant.js';

const Graph = X6.Graph;
const DataUri = X6.DataUri;
const Scroller = X6PluginScroller.Scroller;
const Export = X6PluginExport.Export;


let graph;
// 绘图对象
let draw;
// 节点宽度
let nodeWidth = NODE_WIDTH;
// 显示模式
let displayMode;

/**
 * 初始化图表
 */
export function initGraph(initDisplayMode) {
    displayMode = initDisplayMode || displayMode;

    // -------------------------------
    // 1. 注册端口布局 (ER 端口布局)
    // -------------------------------
    Graph.registerPortLayout(
        'erPortPosition',
        (portsPositionArgs) => {
            return portsPositionArgs.map((_, index) => ({
                position: {
                    x: 0,
                    y: HEAD_LINE_HEIGHT + index * PORT_LINE_HEIGHT,
                },
                angle: 0,
            }));
        },
        true
    );

    // -------------------------------
    // 2. 注册 ER 节点（er-rect）
    // -------------------------------
    Graph.registerNode(
        'er-rect',
        {
            inherit: 'rect',
            // 节点结构：背景矩形 + 文本
            markup: [
                {tagName: 'rect', selector: 'body'},
                {tagName: 'rect', selector: 'head'},
                {tagName: 'text', selector: 'label'},
                {tagName: 'text', selector: 'toggle'}  // 新增切换图标
            ],
            attrs: {
                body: {
                    strokeWidth: 1,
                    stroke: '#5F95FF',
                    fill: '#5F95FF',
                },
                head: {
                    strokeWidth: 1,
                    stroke: '#5F95FF',
                    fill: '#5F95FF',
                    height: HEAD_LINE_HEIGHT,
                    width: nodeWidth,
                },
                label: {
                    event: 'label:dblclick',
                    fontWeight: 'bold',
                    fill: '#fff',
                    fontSize: 12,
                    refX: '50%',
                    refY: HEAD_LINE_HEIGHT / 2,
                    textWrap: {
                        width: -24,
                        height: HEAD_LINE_HEIGHT,
                        ellipsis: true,  // 文本超出显示范围时，自动添加省略号
                        breakWord: true, // 是否截断单词
                    }
                },
                // 定义 toggle 元素的样式及位置（这里将其放在节点右上角）
                toggle: {
                    text: '-', // 默认展开状态显示“-”
                    fontSize: 12,
                    fill: '#fff',
                    ref: 'body',
                    refX: '100%',  // 相对于 body 右侧
                    refY: 5,
                    // 使用适当的偏移，让图标不超出边界
                    x: -8,
                    cursor: 'pointer',
                    // 可加一个标识类，便于事件判断
                    event: 'node:toggle'
                }
            },
            ports: {
                groups: {
                    list: {
                        // 每个端口显示背景矩形、属性名称和属性类型
                        markup: [
                            {tagName: 'rect', selector: 'portBody'},
                            {tagName: 'text', selector: 'portNameLabel'},
                            {tagName: 'text', selector: 'portTypeLabel'},
                        ],
                        attrs: {
                            portBody: {
                                height: PORT_LINE_HEIGHT,
                                strokeWidth: 1,
                                stroke: '#5F95FF',
                                fill: '#EFF4FF',
                                //magnet: true,
                            },
                            portNameLabel: {
                                event: 'portNameLabel:dblclick',
                                ref: 'portBody',
                                refX: 6,
                                refY: 6,
                                fontSize: 10,
                                fill: '#000',
                                textWrap: {
                                    ellipsis: true,  // 文本超出显示范围时，自动添加省略号
                                    breakWord: true, // 是否截断单词
                                },

                            },
                            portTypeLabel: {
                                event: 'portTypeLabel:dblclick',
                                ref: 'portBody',
                                refY: 6,
                                fontSize: 10,
                                fill: '#888',
                                textWrap: {
                                    ellipsis: true,  // 文本超出显示范围时，自动添加省略号
                                    breakWord: true, // 是否截断单词
                                },
                            },
                        },
                        // 指定使用自定义的端口布局
                        position: 'erPortPosition',
                    },
                },
            },
        },
        true
    );


    // -------------------------------
    // 3. 创建 X6 图实例
    // -------------------------------
    graph = new Graph({
        container: document.getElementById('container'),
        grid: {
            size: 10,
            visible: true,
            type: 'dot',
            args: {
                color: '#a0a0a0', // 网格线/点颜色
                thickness: 1,     // 网格线宽度/网格点大小
            },
        },
        background: {color: '#F5F5F5'},

        // 连接配置（本示例中只用于绘制连线）
        connecting: {
            router: {
                name: 'er',
                args: {
                    offset: 25,
                    direction: 'H',
                },
            },
        },
    });

    graph.use(
        new Scroller({
            enabled: true,
        }),
    )
    graph.use(new Export())

    // -------------------------------
    // 6. 节点收起展开
    // -------------------------------
    graph.on('node:toggle', ({e, cell}) => {
        // 阻止后续事件
        e.stopPropagation();
        toggleCollapse(cell);
    });

    // 定义双击时间间隔（毫秒）
    const DOUBLE_CLICK_INTERVAL = 300;
    // 用于存储上次单击的时间和对象
    let lastClickTime = 0;
    let lastClickedView = null;

    // 处理模拟双击事件
    const handleSimulatedDoubleClick = (e, view) => {
        const currentTime = Date.now();
        const isDoubleClick = currentTime - lastClickTime < DOUBLE_CLICK_INTERVAL && view === lastClickedView;

        if (isDoubleClick) {
            //const text = view.selectors[selector].attributes.text.nodeValue;
            const text = e.currentTarget.attributes.text.nodeValue;
            copyToClipboard(text);
            // 重置状态
            lastClickTime = 0;
            lastClickedView = null;
        } else {
            lastClickTime = currentTime;
            lastClickedView = view;
        }
    };

    // 处理双击事件
    graph.on('label:dblclick', ({ view, e }) => {
        handleSimulatedDoubleClick(e, view);
    });

    graph.on('portNameLabel:dblclick', ({ view, e }) => {
        handleSimulatedDoubleClick(e, view);
    });

    graph.on('portTypeLabel:dblclick', ({ view, e }) => {
        handleSimulatedDoubleClick(e, view);
    });

}

function copyToClipboard(text) {
    console.log('复制文本: \n' + text);
    navigator.clipboard.writeText(text)
        .then(() => {
            // 显示复制成功提示
            showCopySuccessMessage();
        })
        .catch((err) => {
            console.error('复制失败:', err);
        });
}

function showCopySuccessMessage() {
    const message = document.getElementById('copySuccessMessage');
    message.classList.add('show');
    setTimeout(() => {
        message.classList.remove('show');
    }, 1500);
}

/**
 * 绘制图形
 * @param {Object} newDraw - 新的绘图数据
 */
export function drawGraph(newDraw) {
    draw = newDraw;
    graph.clearCells()

    draw.nodes.forEach(node => {
        addNode(node);
    });

    draw.edges.forEach(edge => {
        addEdge(edge);
    });
}


function addNode(node) {
    graph.addNode({
        id: node.id,
        shape: 'er-rect',
        x: node.x,
        y: node.y,
        width: nodeWidth,
        height: node.height,
        attrs: {
            label: {
                text: getNodeLabelText(node),
                title: node.title,
            },
        },
        ports: {items: getPortProps(node)},
        data: {node}, // 保存 node 数据到节点中
    });
}

function addEdge(edge) {
    graph.addEdge({
        source: {
            cell: edge.sourceNodeId,
            port: edge.sourcePortId,
        },
        target: {
            cell: edge.targetNodeId,
            selector: 'head',
        },
        labels: [
            {
                attrs: {
                    label: {
                        text: edge.centerText,
                        title: edge.title,
                    },
                },
            },
        ],
        attrs: {
            line: {
                stroke: '#A2B1C3',
                strokeWidth: 2,
            },
        },
    });
}

function toggleCollapse(node) {
    // 获取当前状态（假设在 cell.data 中存储 collapsed 标识）
    const data = node.getData() || {};
    const collapsed = data.collapsed;
    if (collapsed) {
        // 当前为收起状态，进行展开
        // 恢复节点高度, 更新状态及 toggle 图标文本
        node.prop({
            size: {height: data.originalHeight},
            data: {...data, collapsed: false},
            attrs: {
                toggle: {text: '-'},
            }
        });

        // 显示从该节点的Port
        node.addPorts(getPortProps(data.node));

        // 显示从该节点出发的边
        draw.edges.filter(edge => edge.sourceNodeId === node.id).forEach(edge => addEdge(edge));

    } else {
        // 当前为展开状态，进行收起
        // 保存当前高度（原本计算的高度）和端口数据，方便恢复
        const currentHeight = node.size().height;
        // 修改节点高度，只保留标题部分, 更新 toggle 图标文本
        node.prop({
            size: {height: HEAD_LINE_HEIGHT},
            data: {...data, collapsed: true, originalHeight: currentHeight},
            attrs: {
                toggle: {text: '+'},
            }
        });

        // 移除从该节点出发的边
        const edges = graph.getOutgoingEdges(node);
        if (edges) {
            edges.forEach(edge => {
                graph.removeEdge(edge);
            });
        }

        // 移除Ports
        node.removePorts();
    }
}

function getPortProps(node) {
    // 计算Port矩形宽度
    const space1 = 6;
    const nameWidth = nodeWidth * 3 / 5 - space1;
    const space2 = 6
    const typeRefX = space1 + nameWidth + space2;
    const space3 = 6
    const typeWidth = nodeWidth - space1 - nameWidth - space2 - space3;

    return node.ports.map(port => ({
        id: port.id,
        group: 'list',
        attrs: {
            portBody: {title: port.title, width: nodeWidth},
            portNameLabel: {
                text: port.properties[displayMode.mainPortPropertyName], textWrap: {width: nameWidth},
                title: port.title
            },
            portTypeLabel: {
                text: port.properties[displayMode.secondPortPropertyName], textWrap: {width: typeWidth},
                title: port.title,
                refX: typeRefX
            },
        },
        data: {port}
    }));
}

function getNodeLabelText(node) {
    let text = node.properties[displayMode.nodePropertyName];
    if (displayMode.secondNodePropertyName) {
        text = text + '\n' + node.properties[displayMode.secondNodePropertyName];
    }
    return text;
}

// 更新节点宽度的函数
export function updateNodeWidth(newWidth) {
    nodeWidth = newWidth;

    // 遍历所有节点更新宽度
    graph.getNodes().forEach(node => {
        // 调整节点整体大小

        const collapsed = node.data.collapsed;

        if (!collapsed) {
            node.prop({
                size: {width: newWidth},
                ports: {items: getPortProps(node.data.node)}
            });
        } else {
            node.prop({
                size: {width: newWidth},
            });
        }
    });
}

export function downloadPNG() {
    graph.exportPNG('模型图',
        {
            padding: {
                top: 20,
                right: 20,
                bottom: 20,
                left: 20,
            },
        });
}

export function downloadDrawio() {
    downloadDrawioFile(graph);
}

export function resize(width, height) {
    graph.resize(width, height);
}

/**
 * 根据 mode 更新所有节点及端口的 label 文本
 */
export function updateDisplayMode(newDisplayMode) {
    displayMode = newDisplayMode

    graph.getNodes().forEach(node => {
        const data = node.getData();
        if (data && data.node) {
            const collapsed = data.collapsed;
            const drawNode = data.node;

            if (!collapsed) {

                node.prop({
                    attrs: {label: {text: getNodeLabelText(drawNode)}},
                    ports: {items: getPortProps(drawNode)}
                });
            } else {
                node.attr({label: {text: getNodeLabelText(drawNode)}});
            }
        }
    });
}