document.getElementById('searchForm').addEventListener('submit', function(e) {
    e.preventDefault(); // 폼 기본 동작 차단(새로고침)
    const query = document.getElementById('searchInput').value.trim();
    if (!query) return;
    window.open('https://www.google.com/search?q=' + encodeURIComponent(query), '_blank');
});