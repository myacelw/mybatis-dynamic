document.addEventListener("DOMContentLoaded", function() {
    // Only apply translation if URL contains /zh/
    if (window.location.pathname.indexOf('/zh/') === -1) {
        return;
    }

    var map = {
        'Home': '首页',
        'Quick Start': '快速开始',
        'Article': '深度文章',
        'Core Features': '核心功能',
        'Introduction': '简介',
        'DataManager & ID': 'DataManager 与 ID',
        'ModelService': 'ModelService',
        'DataChangeInterceptor': '数据变更拦截器',
        'PermissionGetter & CurrentUserHolder': '权限管理',
        'Spring Integration': 'Spring 集成',
        'Advanced Features': '高级功能',
        'Overview': '概览',
        'Fluent Chain APIs': '流畅链式 API',
        'Join Operations': '连接操作',
        'Recursive & Tree': '递归与树形结构',
        'Relation Filling': '关联填充',
        'ModelService Advanced': 'ModelService 高级功能',
        'Custom Conditions': '自定义条件',
        'Best Practices': '最佳实践',
        'Extensions': '扩展'
    };

    var links = document.querySelectorAll('.md-nav__link');
    links.forEach(function(link) {
        var text = link.textContent.trim();
        if (map[text]) {
            link.textContent = map[text];
        }
    });
});
