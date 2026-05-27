package org.acme.login;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import io.vertx.ext.web.RoutingContext;

import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

@Path("/")
public class AuthResource {

    @Inject
    RoutingContext context;

    // GET / → 세션 유무에 따라 메인 페이지 분기
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response mainPage() {
        String loginUser = context.session().get("loginUser");

        System.out.println("=== [GET /] 세션 ID : " + context.session().id());
        System.out.println("=== [GET /] loginUser : " + loginUser);

        String htmlPath = loginUser != null
                ? "META-INF/resources/login/main_after_login.html"
                : "META-INF/resources/main_index.html";

        InputStream html = getClass()
                .getClassLoader()
                .getResourceAsStream(htmlPath);

        return Response.ok(html).build();
    }

    // GET /login → 로그인 HTML 페이지 반환
    @GET
    @Path("/login")
    @Produces(MediaType.TEXT_HTML)
    public Response loginPage() {
        InputStream html = getClass()
                .getClassLoader()
                .getResourceAsStream("META-INF/resources/login/login.html");

        return Response.ok(html).build();
    }

    // POST /login_check → 로그인 처리
    @POST
    @Path("/login_check")
    @Transactional
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response loginCheck(
            @FormParam("username") String username,
            @FormParam("password") String password) {

        User user = User.findByUsername(username);

        if (user == null || !user.password.equals(password)) {
            return Response
                    .seeOther(URI.create("/login?error=1"))
                    .build();
        }

        // 세션에 로그인 정보 저장
        context.session().put("loginUser", username);

        return Response
                .seeOther(URI.create("/after_login"))
                .build();
    }

    // GET /after_login → 로그인 후 페이지
    @GET
    @Path("/after_login")
    @Produces(MediaType.TEXT_HTML)
    public Response afterLogin() {
        String loginUser = context.session().get("loginUser");

        System.out.println("=== 세션 ID : " + context.session().id());
        System.out.println("=== loginUser : " + loginUser);

        if (loginUser == null) {
            return Response
                    .seeOther(URI.create("/login"))
                    .build();
        }

        InputStream html = getClass()
                .getClassLoader()
                .getResourceAsStream("META-INF/resources/login/main_after_login.html");

        return Response.ok(html).build();
    }

    // GET /logout → 로그아웃
    @GET
    @Path("/logout")
    public Response logout() {
        System.out.println("=== 로그아웃 전 세션 ID : " + context.session().id());
        System.out.println("=== 로그아웃 전 loginUser : " + context.session().get("loginUser"));

        // 세션 전체 삭제
        context.session().destroy();

        return Response
                .seeOther(URI.create("/"))
                .build();
    }

    // GET /register → 회원가입 페이지
    @GET
    @Path("/register")
    @Produces(MediaType.TEXT_HTML)
    public Response registerPage() {
        InputStream html = getClass()
                .getClassLoader()
                .getResourceAsStream("META-INF/resources/login/register.html");

        return Response.ok(html).build();
    }

    // POST /register_check → 회원가입 처리
    @POST
    @Path("/register_check")
    @Transactional
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    public Response registerCheck(
            @FormParam("username") String username,
            @FormParam("password") String password,
            @FormParam("email") String email,
            @FormParam("phone") String phone) {

        // ① 아이디 중복 체크
        if (User.findByUsername(username) != null) {
            return Response
                    .seeOther(URI.create("/register?error=duplicate_username"))
                    .build();
        }

        // ② 이메일 중복 체크
        if (User.findByEmail(email) != null) {
            return Response
                    .seeOther(URI.create("/register?error=duplicate_email"))
                    .build();
        }

        // ③ DB 삽입
        User newUser = new User();
        newUser.username = username;
        newUser.password = password;
        newUser.email = email;
        newUser.phone = phone;
        newUser.persist();

        // ④ 가입 완료 페이지로 이동
        return Response
                .seeOther(URI.create("/register_success"))
                .build();
    }

    // GET /register_success → 회원가입 성공 페이지
    @GET
    @Path("/register_success")
    @Produces(MediaType.TEXT_HTML)
    public Response registerSuccess() {
        InputStream html = getClass()
                .getClassLoader()
                .getResourceAsStream("META-INF/resources/login/register_success.html");

        return Response.ok(html).build();
    }

    // GET /profile → 프로필 페이지
    @GET
    @Path("/profile")
    @Produces(MediaType.TEXT_HTML)
    public Response profilePage() {
        // ① 세션 체크
        String loginUser = context.session().get("loginUser");

        if (loginUser == null) {
            return Response
                    .seeOther(URI.create("/login"))
                    .build();
        }

        // ② DB에서 사용자 정보 조회
        User user = User.findByUsername(loginUser);

        if (user == null) {
            context.session().destroy();

            return Response
                    .seeOther(URI.create("/login"))
                    .build();
        }

        // ③ 세션에 사용자 정보 저장
        context.session().put("userEmail", user.email);
        context.session().put("userPhone", user.phone);
        context.session().put(
                "profileImage",
                user.profileImage != null ? user.profileImage : "default.png");

        // ④ 프로필 페이지 반환
        InputStream html = getClass()
                .getClassLoader()
                .getResourceAsStream("META-INF/resources/login/profile.html");

        return Response.ok(html).build();
    }

    // GET /profile/info → 프로필 정보 JSON
    @GET
    @Path("/profile/info")
    @Produces(MediaType.APPLICATION_JSON)
    public Response profileInfo() {
        // ① 세션 체크
        String loginUser = context.session().get("loginUser");

        if (loginUser == null) {
            return Response.status(401).build();
        }

        // ② DB 조회
        User user = User.findByUsername(loginUser);

        if (user == null) {
            return Response.status(404).build();
        }

        // ③ JSON 응답
        return Response.ok(
                Map.of(
                        "username", user.username,
                        "email", user.email != null ? user.email : "",
                        "phone", user.phone != null ? user.phone : "",
                        "profileImage", user.profileImage != null ? user.profileImage : ""))
                .build();
    }

    // POST /profile/upload → 프로필 이미지 업로드
    @POST
    @Path("/profile/upload")
    @Transactional
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response profileUpload(
            @RestForm("profileImage") FileUpload file) {

        // ① 세션 체크
        String loginUser = context.session().get("loginUser");

        if (loginUser == null) {
            return Response
                    .seeOther(URI.create("/login"))
                    .build();
        }

        // ② 파일 선택 여부 체크
        if (file == null || file.fileName() == null || file.fileName().isBlank()) {
            return Response
                    .seeOther(URI.create("/profile?error=no_file"))
                    .build();
        }

        try {
            // ③ 확장자 검사
            String original = file.fileName();

            int dotIndex = original.lastIndexOf(".");
            if (dotIndex == -1) {
                return Response
                        .seeOther(URI.create("/profile?error=invalid_type"))
                        .build();
            }

            String ext = original.substring(dotIndex + 1).toLowerCase();

            if (!ext.matches("jpg|jpeg|png|gif|webp")) {
                return Response
                        .seeOther(URI.create("/profile?error=invalid_type"))
                        .build();
            }

            // ④ 파일 크기 검사, 5MB
            if (file.size() > 5 * 1024 * 1024) {
                return Response
                        .seeOther(URI.create("/profile?error=too_large"))
                        .build();
            }

            // ⑤ UUID 파일명 생성
            String newFileName = UUID.randomUUID().toString() + "." + ext;

            // ⑥ 저장 폴더 생성
            java.nio.file.Path uploadDir = Paths.get(
                    "src/main/resources/META-INF/resources/uploads/profile");

            Files.createDirectories(uploadDir);

            // ⑦ 파일 저장
            Files.copy(
                    file.uploadedFile(),
                    uploadDir.resolve(newFileName),
                    StandardCopyOption.REPLACE_EXISTING);

            // ⑧ DB 업데이트
            User user = User.findByUsername(loginUser);

            if (user == null) {
                return Response
                        .seeOther(URI.create("/login"))
                        .build();
            }

            user.profileImage = newFileName;

            return Response
                    .seeOther(URI.create("/profile"))
                    .build();

        } catch (Exception e) {
            e.printStackTrace();

            return Response
                    .seeOther(URI.create("/profile?error=upload_fail"))
                    .build();
        }
    }
}