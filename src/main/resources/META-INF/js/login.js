function validateAndLogin() {
    // letvalid= true;
    // constusername= document.getElementById('usernameInput').value.trim();
    // constpassword= document.getElementById('passwordInput').value;
    
    // // ①아이디유효성검사
    // // 조건: 4~20자영문/숫자만허용
    // // 정규식: /^[a-zA-Z0-9]{4,20}$/
    // // 실패시: showError('usernameInput', 'usernameMsg', '오류메시지')
    // // 성공시: clearError('usernameInput')
    // /* 여기에코드를작성하시오*/

    // // ②패스워드유효성검사
    // // 조건: 8자이상, 영문+ 숫자+ 특수문자(!@#$%^&*) 포함
    // // 정규식: /^(?=.*[a-zA-Z])(?=.*\d)(?=.*[!@#$%^&*]).{8,}$/
    // // 실패시: showError('passwordInput', 'passwordMsg', '오류메시지')
    // // 성공시: clearError('passwordInput')
    // /* 여기에코드를작성하시오*/

    // // ③두항목모두통과시로그인실행
    // if(valid) submitLogin();
     submitLogin(); // 유효성 검사(지난 주 문제)
}
async function submitLogin() {
    const password = document.getElementById('passwordInput').value;
    const hashed = await hashPassword(password);
    document.getElementById('password').value = hashed;
    document.getElementById('loginForm').submit();
}
