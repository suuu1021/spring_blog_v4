package com.tenco.blog.user;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@RequiredArgsConstructor // DI 처리
@Controller
public class UserController {

    private final UserRepository userRepository;
    // httpSession <--- 세션 메모리에 접근을 할 수 있음
    private final HttpSession httpSession;

    // 회원 정보 수정 화면
    // 주소 설계 : http://localhost:8080/user/update-form
    @GetMapping("/user/update-form")
    public String updateForm(HttpServletRequest request, HttpSession session) {
        // 1. 로그인 체크
        User sessionUser = (User) session.getAttribute("sessionUser");
        if (sessionUser == null) {
            return "redirect:/login-form";
        }
        request.setAttribute("user", sessionUser);
        return "user/update-form";
    }

    // 회원 정보 수정 액션 처리
    @PostMapping("/user/update")
    public String update(UserRequest.UpdateDTO reqDTO,
                         HttpSession session, HttpServletRequest request) {
        User sessionUser = (User) session.getAttribute("sessionUser");
        if (sessionUser == null) {
            return "redirect:/login-form";
        }
        // 데이터 유효성 검사 처리
        reqDTO.validate();
        // 회원 정보 수정은 권한 체크가 필요 없다 (세션에서 정보를 가져오기 때문)
        User updateUser = userRepository.updateById(sessionUser.getId(), reqDTO);
        // 세션 동기화
        session.setAttribute("sessionUser", updateUser);
        // 다시 회원 정보 보기 화면 요청 - GET 요청
        return "redirect:/user/update-form";
    }

    /**
     * 회원 가입 화면 요청
     *
     * @return join-form.mustache
     */
    @GetMapping("/join-form")
    public String joinForm() {
        return "user/join-form";
    }

    // 회원 가입 액션 처리
    @PostMapping("/join")
    public String join(UserRequest.JoinDTO joinDTO, HttpServletRequest request) {
        System.out.println("=== 회원가입 요청 ===");
        System.out.println("사용자 명: " + joinDTO.getUsername());
        System.out.println("사용자 이메일: " + joinDTO.getEmail());

        try {
            // 1. 입력된 데이터 검증 (유효성 검사)
            joinDTO.validate();
            // 2. 사용자 명 중복 체크
            User exsistUser = userRepository.findByUsername(joinDTO.getUsername());
            if (exsistUser != null) {
                throw new IllegalArgumentException("이미 존재하는 사용자명 입니다" + joinDTO.getUsername());
            }
            // 3. DTO를 User Object로 변환
            User user = joinDTO.toEntity();
            // 4. User Object를 영속화 처리
            userRepository.save(user);
            return "redirect:/login-form";
        } catch (Exception e) {
            // 검증 실패 시 보통 에러 메세지와 함께 다시 폼에 전달
            request.setAttribute("errorMessage", "잘못된 요청입니다");
            return "user/join-form";
        }
    }

    /**
     * 로그인 화면 요청
     *
     * @return login-form
     */
    @GetMapping("/login-form")
    public String loginForm() {
        // 반환값이 뷰(파일) 이름이 됨 (뷰 리졸버가 실제 파일 경로를 찾아 감)
        return "user/login-form";
    }

    // 로그인 액션 처리
    // 자원의 요청은 GET 방식이다. 단, 로그인 요청은 예외
    // 보안상의 이유 (주소창에 username,password가 띄워짐, 히스토리에 저장됨)

    // DTO 패턴 활용
    // 1. 입력 데이터 검증
    // 2. 사용자명과 비밀번호를 DB에 접근해서 조회
    // 3. 로그인 성공/실패 처리
    // 4. 로그인 성공 -> 서버 측 메모리에 사용자 정보 저장
    // 5. 메인 화면으로 리다이렉트
    @PostMapping("/login")
    public String login(UserRequest.LoginDTO loginDTO) {
        System.out.println("=== 로그인 시도 ===");
        System.out.println("사용자 명 : " + loginDTO.getUsername());

        try {
            // 1.
            loginDTO.validate();
            // 2.
            User user = userRepository.findByUsernameAndPassword(loginDTO.getUsername(), loginDTO.getPassword());
            // 3. 로그인 실패 : 일치된 사용자 없음
            if (user == null) {
                throw new IllegalArgumentException("사용자 명 또는 비밀번호가 일치하지 않습니다.");
            }
            // 4. 로그인 성공
            httpSession.setAttribute("sessionUser", user);
            // 5. 로그인 성공 후 리스트 페이지 이동
            return "redirect:/";

        } catch (Exception e) {
            // 필요하다면 에러 메세지 생성해서 내려 보내기
            return "user/login-form";
        }
    }

    // 로그아웃 처리
    @GetMapping("/logout")
    public String logout() {
        // 세션 무효화
        httpSession.invalidate();
        // "redirect: " 스프링에서 접두사를 사용하면 다른 URL로 리다이렉트 됨
        // 즉 리다이렉트 한다는 것은 뷰를 렌더링 하지 않고 브라우저가 재요청을
        // 다시 하게끔 유도 한다.
        return "redirect:/";
    }

}
