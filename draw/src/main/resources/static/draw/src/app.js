// src/app.js
import {fetchDisplayModes, fetchEntityData, fetchModuleGroups} from './dataService.js';
import {
    downloadDrawio,
    downloadPNG,
    drawGraph,
    initGraph,
    resize,
    updateDisplayMode,
    updateNodeWidth
} from './graph.js';
import {HEAD_LINE_HEIGHT, NODE_WIDTH, PORT_LINE_HEIGHT} from './constant.js';

async function main() {
    const displayModeEle = document.getElementById("displayMode");
    const nodeWidthInput = document.getElementById('nodeWidthInput');
    const updateWidthButton = document.getElementById('updateWidthButton');
    const saveGraphButton = document.getElementById('saveGraphButton');
    const saveDrawioButton = document.getElementById('saveDrawioButton');

    nodeWidthInput.value = NODE_WIDTH;

    try {
        // ==== 显示模式 ====
        const displayModes = await fetchDisplayModes();

        //插入option 列表
        displayModes.forEach(t => {
            const option = document.createElement("option");
            option.value = t.name;
            option.text = t.name;
            displayModeEle.appendChild(option);
        });

        // -------------------------------
        // 从 data/draw.json 中获取绘图信息（也可以替换为其他 URL）
        // 初始化 ER 图绘制
        initGraph(displayModes[0]);
        drawGraph(await fetchEntityData(null, HEAD_LINE_HEIGHT, PORT_LINE_HEIGHT, nodeWidthInput.value))

        // -------------------------------
        // ==== 模块组处理 ====
        const moduleGroups = await fetchModuleGroups();
        const moduleGroupEle = document.getElementById("moduleGroup");
        //插入option 列表
        moduleGroups.forEach(t => {
            const option = document.createElement("option");
            option.value = t;
            option.text = t;
            moduleGroupEle.appendChild(option);
        });

        // -------------------------------
        // 7. 工具条：切换节点和端口 label 显示模式
        // -------------------------------
        moduleGroupEle.addEventListener("change", async e => {
            //找到显示模式
            const moduleGroup = e.target.value;
            // 从 data/draw.json 中获取绘图信息（也可以替换为其他 URL）
            // ER 图绘制
            drawGraph(await fetchEntityData(moduleGroup, HEAD_LINE_HEIGHT, PORT_LINE_HEIGHT, nodeWidthInput.value));
        });

        displayModeEle.addEventListener("change", e => {
            //找到显示模式
            const displayMode = displayModes.find(t => t.name === e.target.value) || displayModes[0];
            updateDisplayMode(displayMode);
        });

        // 保存图片
        saveGraphButton.addEventListener('click', () => {
            downloadPNG()
        });

        // 保存为draw.io文件
        saveDrawioButton.addEventListener('click', () => {
            downloadDrawio();
        });

        // 监听工具条中更新节点宽度的按钮点击事件
        function updateNodeWidthHandle() {
            const newWidth = parseInt(nodeWidthInput.value, 10);
            if (isNaN(newWidth) || newWidth < 50 || newWidth > 500) {
                alert('请输入50到500间的数字。');
                return;
            }
            updateNodeWidth(newWidth);
        }

        // 监听按钮点击事件
        updateWidthButton.addEventListener('click', () => {
            updateNodeWidthHandle();
        });

        // 监听输入框的 keyup 事件
        nodeWidthInput.addEventListener('keyup', (e) => {
            if (e.key === 'Enter') {
                updateNodeWidthHandle();
            }
        });

        // 监听窗口大小变化，自动调整画布大小
        window.addEventListener('resize', () => {
            const container = document.getElementById('main');
            const width = container.clientWidth;
            const height = container.clientHeight;
            resize(width, height);
        });

    } catch (error) {
        console.error('获取实体数据失败：', error);
    }
}

main();
