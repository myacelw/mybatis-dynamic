/**
 * 将 X6 图导出为 draw.io (mxGraphModel) 格式的 XML 字符串，
 * 包括节点、节点内的 port 以及边。所有 mxCell 均在根级输出，
 * 通过 parent 属性建立层级关系。
 * @param {Graph} graph - AntV X6 图实例
 * @returns {string} draw.io 格式的 XML 字符串
 */
function exportToDrawioXML(graph) {
    const LINE_HEIGHT = 24; // 与 X6 中 port 的高度保持一致

    let xml = '<?xml version="1.0" encoding="UTF-8"?>\n';
    xml += '<mxGraphModel dx="0" dy="0" grid="1" gridSize="10" guides="1" tooltips="1" connect="1" arrows="1" fold="1" page="1" pageScale="1" pageWidth="827" pageHeight="1169">\n';
    xml += '  <root>\n';
    xml += '    <mxCell id="0"/>\n';
    xml += '    <mxCell id="1" parent="0"/>\n';

    // 用于保存所有 port 的 mxCell（稍后追加到根下）
    let portCells = '';

    // 遍历所有节点，生成节点的 mxCell
    graph.getNodes().forEach(node => {
        const id = node.id;
        // 优先从 label/text 取文本，如果为空则尝试 textWrap，再尝试 title
        let nodeLabel = node.attr('label/text');
        if (!nodeLabel) {
            const labelWrap = node.attr('label/textWrap');
            nodeLabel = labelWrap ? labelWrap.text : '';
        }
        if (!nodeLabel) {
            nodeLabel = node.attr('label/title') || '';
        }
        const pos = node.position();
        const size = node.size();
        xml += `    <mxCell id="${id}" value="${escapeXml(nodeLabel)}" style="swimlane;fontStyle=0;align=center;verticalAlign=top;childLayout=stackLayout;horizontal=1;startSize=26;horizontalStack=0;resizeParent=1;resizeLast=0;collapsible=1;marginBottom=0;rounded=0;shadow=0;strokeWidth=1;fillColor=#dae8fc;strokeColor=#6c8ebf;" vertex="1" parent="1">\n`;
        xml += `      <mxGeometry x="${pos.x}" y="${pos.y}" width="${size.width}" height="${size.height}" as="geometry"/>\n`;
        xml += '    </mxCell>\n';

        // 遍历该节点下的所有 port，将 port 作为独立 mxCell 输出，parent 设置为当前节点 id
        const ports = node.getPorts();
        ports.forEach((port, index) => {
            // 组合 portNameLabel 和 portTypeLabel，之间用 " : " 分隔
            const leftText = getDisplayText(port.attrs.portNameLabel);
            const rightText = getDisplayText(port.attrs.portTypeLabel);
            let portLabel = leftText;
            if (rightText) {
                portLabel += ' : ' + rightText;
            }
            portCells += `    <mxCell id="${id}-${port.id}" value="${escapeXml(portLabel)}" style="text;align=left;verticalAlign=top;spacingLeft=4;spacingRight=4;overflow=hidden;rotatable=0;points=[[0,0.5],[1,0.5]];portConstraint=eastwest;rounded=0;shadow=0;html=0;" vertex="1" parent="${id}">\n`;
            // 假定 x 为 0，y 为 (index+1)*LINE_HEIGHT，宽度为节点宽度，高度为 LINE_HEIGHT
            portCells += `      <mxGeometry x="0" y="${(index + 1) * LINE_HEIGHT}" width="${size.width}" height="${LINE_HEIGHT}" as="geometry"/>\n`;
            portCells += '    </mxCell>\n';
        });
    });

    // 追加所有 port mxCell 到 XML 中
    xml += portCells;

    // 遍历所有边，生成 mxCell，并处理连线上的说明文字及起始位置为 port
    graph.getEdges().forEach(edge => {
        const id = edge.id;
        // 处理边上的文字
        const labels = edge.getLabels();
        let edgeLabel = "";
        if (labels && labels.length > 0) {
            edgeLabel = labels.map(label => {
                let txt = label.attrs && label.attrs.label && label.attrs.label.text;
                if (!txt && label.attrs && label.attrs.label && label.attrs.label.textWrap) {
                    txt = label.attrs.label.textWrap.text;
                }
                return txt || "";
            }).join(" ");
        } else {
            edgeLabel = edge.attr('label/text');
            if (!edgeLabel) {
                const labelWrap = edge.attr('label/textWrap');
                edgeLabel = labelWrap ? labelWrap.text : '';
            }
        }
        // 获取源/目标节点和端口信息
        const sourceNodeId = edge.getSourceCellId();
        const sourcePortId = edge.getSourcePortId();
        const targetNodeId = edge.getTargetCellId();
        const targetPortId = edge.getTargetPortId();
        let sourceId = sourceNodeId;
        if (sourcePortId) {
            sourceId = `${sourceNodeId}-${sourcePortId}`;
        }
        let targetId = targetNodeId;
        if (targetPortId) {
            targetId = `${targetNodeId}-${targetPortId}`;
        }
        xml += `    <mxCell id="${id}" value="${escapeXml(edgeLabel)}" style="edgeStyle=orthogonalEdgeStyle;rounded=0;html=1;" edge="1" parent="1" source="${sourceId}" target="${targetId}">\n`;
        xml += '      <mxGeometry relative="1" as="geometry"/>\n';
        xml += '    </mxCell>\n';
    });

    xml += '  </root>\n';
    xml += '</mxGraphModel>';
    return xml;
}

/**
 * 简单的 XML 转义函数
 * @param {string} str
 * @returns {string}
 */
function escapeXml(str) {
    if (!str) return '';
    return str.replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&apos;');
}

/**
 * 辅助函数：从属性对象中获取显示文本
 * @param {object} attrObj
 * @returns {string}
 */
function getDisplayText(attrObj) {
    if (!attrObj) return '';
    if (attrObj.text) return attrObj.text;
    if (attrObj.textWrap && attrObj.textWrap.text) return attrObj.textWrap.text;
    return '';
}

/**
 * 触发下载 draw.io 文件
 * @param {Graph} graph - AntV X6 图实例
 */
export function downloadDrawioFile(graph) {
    const xmlContent = exportToDrawioXML(graph);
    const blob = new Blob([xmlContent], { type: 'text/xml;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = '模型图.drawio';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
}
