layui.use('layer', function(){
    var layer = layui.layer;
    document.getElementById('search-btn').addEventListener('click', function(){
        search();
    });
    document.getElementById('keyword').onkeydown = function(e) {
        if (e.keyCode == 13)
            search();
    }
});

function search() {
    var keyword = document.getElementById('keyword').value;
    if (keyword.trim() == '') {
        layer.msg("搜索内容不能为空！");
        return;
    }
    window.location.href = '/?fileName=' + keyword;
}