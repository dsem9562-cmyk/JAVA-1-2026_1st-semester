window.onload= function() {
    fetch('/profile/info') // 서버에서사용자정보요청, 비동기i/o
        .then(res => res.json()) // json파싱
        .then(data => {
            document.getElementById('infoUsername').textContent
                = data.username; // DOM 조작방지
            document.getElementById('infoEmail').textContent
                = data.email;
            document.getElementById('infoPhone').textContent
                = data.phone;
            if (data.profileImage) { // null 체크
                document.getElementById('profileImg').src
                    = '/uploads/profile/' + data.profileImage;
            }
        });
        // 회원 정보 읽고, json 형태 변환 후 화면 갱신(비동기 처리)
        fetch('/profile/info')
        .then(res => res.json())
        .then(data => {
            const profileLink = document.getElementById('profileNavLink');
            if (profileLink) {
                profileLink.setAttribute('data-bs-title', ' ' + data.username);
                new bootstrap.Tooltip(profileLink);
        }
})
}