# Specification: Frontend Internationalization (Chinese to English)

## Goal
Refactor the frontend resources in the `draw` module (`@draw/src/main/resources/static/draw/`) to replace all Chinese text with English. This aligns the UI language with the codebase's internationalization strategy.

## Scope
The following files will be modified:

1.  **`draw/src/main/resources/static/draw/data/displayModes.json`**
    -   Translate `name` fields (e.g., "类图(名称-类型)" -> "Class Diagram (Name-Type)").

2.  **`draw/src/main/resources/static/draw/index.html`**
    -   Change `lang="zh"` to `lang="en"`.
    -   Translate `<title>` ("模型图" -> "Model Diagram").
    -   Translate toolbar labels ("显示模式:" -> "Display Mode:", "显示模块组:" -> "Module Group:", "节点宽度:" -> "Node Width:").
    -   Translate dropdown options ("全部" -> "All").
    -   Translate buttons ("更新宽度" -> "Update Width", "保存为图片" -> "Save as Image", "保存为draw.io文件" -> "Save as .drawio").
    -   Translate messages ("复制成功！" -> "Copied successfully!").

3.  **`draw/src/main/resources/static/draw/src/app.js`**
    -   Translate alert messages ("请输入50到500间的数字。" -> "Please enter a number between 50 and 500.").
    -   Translate error logs ("获取实体数据失败 ：" -> "Failed to fetch entity data:").

4.  **`draw/src/main/resources/static/draw/src/dataService.js`**
    -   Translate error messages ("网络请求失败 ：" -> "Network request failed:").

5.  **`draw/src/main/resources/static/draw/src/graph.js`**
    -   Translate console logs ("复制文本: 
" -> "Copy text: 
", "复制失败:" -> "Copy failed:").
    -   Translate export filename ("模型图" -> "Model_Diagram").

## Non-Functional Requirements
-   Ensure valid JSON syntax in `displayModes.json`.
-   Ensure no syntax errors in JavaScript files.
-   Maintain existing layout and styling (CSS is not in scope unless text length breaks layout, but "Update Width" should fit).
