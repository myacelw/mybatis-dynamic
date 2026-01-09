// src/dataService.js
export async function fetchEntityData(moduleGroup, headHeight, portHeight, width) {
    const params = new URLSearchParams();
    if (moduleGroup) params.append('moduleGroup', moduleGroup);
    if (headHeight)  params.append('headHeight', headHeight);
    if (portHeight)  params.append('portHeight', portHeight);
    if (width)       params.append('width', width);

    const queryString = params.toString();
    const url = `./data/draw.json${queryString ? '?' + queryString : ''}`;

    const response = await fetch(url);
    if (!response.ok) {
        throw new Error(`网络请求失败：${response.statusText}`);
    }
    return await response.json();
}

export async function fetchDisplayModes() {
    const response = await fetch('./data/displayModes.json');
    if (!response.ok) {
        throw new Error(`网络请求失败：${response.statusText}`);
    }
    return await response.json();
}


export async function fetchModuleGroups() {
    const response = await fetch('./data/moduleGroups.json');
    if (!response.ok) {
        throw new Error(`网络请求失败：${response.statusText}`);
    }
    return await response.json();
}
