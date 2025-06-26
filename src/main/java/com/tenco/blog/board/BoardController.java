package com.tenco.blog.board;

import com.tenco.blog._core.errors.exception.Exception403;
import com.tenco.blog._core.errors.exception.Exception404;
import com.tenco.blog.user.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@RequiredArgsConstructor
@Controller
public class BoardController {

    private static final Logger log = LoggerFactory.getLogger(BoardController.class);
    private final BoardRepository boardRepository;


    @GetMapping("/board/{id}/update-form")
    public String updateForm(@PathVariable(name = "id") Long id,
                             HttpServletRequest request, HttpSession session) {

        log.info("게시글 수정 폼 요청 - id : {}", id);

        User sessionUser = (User) session.getAttribute("sessionUser");

        Board board = boardRepository.findById(id);
        if (board == null) {
            throw new Exception404("게시글이 존재하지 않습니다.");
        }
        if (!board.isOwner(sessionUser.getId())) {
            throw new Exception403("게시글 수정 권한이 없습니다");
        }
        request.setAttribute("board", board);
        return "board/update-form";
    }


    @PostMapping("/board/{id}/update-form")
    public String update(@PathVariable(name = "id") Long id,
                         BoardRequest.UpdateDTO reqDTO,
                         HttpSession session) {

        log.info("게시글 수정 기능 요청 - id : {}, 새 제목 : {}", id, reqDTO.getTitle());

        User sessionUser = (User) session.getAttribute("sessionUser");

        reqDTO.validate();
        Board board = boardRepository.findById(id);
        if (!board.isOwner(sessionUser.getId())) {
            throw new Exception403("게시글 수정 권한이 없습니다");
        }
        boardRepository.updateById(id, reqDTO);
        return "redirect:/board/" + id;
    }


    @PostMapping("/board/{id}/delete")
    public String delete(@PathVariable(name = "id") Long id, HttpSession session) {

        log.info("게시글 삭제 요청 - id : {}", id);

        User sessionUser = (User) session.getAttribute("sessionUser");

        Board board = boardRepository.findById(id);
        if (board == null) {
            throw new Exception404("이미 삭제된 게시글 입니다.");
        }
        if (!board.isOwner(sessionUser.getId())) {
            throw new Exception403("삭제 권한이 없습니다.");
        }
        boardRepository.deleteById(id);
        return "redirect:/";
    }


    @GetMapping("/board/save-form")
    public String saveForm(HttpSession session) {
        log.info("게시글 작성 화면 요청");
        return "board/save-form";
    }


    @PostMapping("/board/save")
    public String save(BoardRequest.SaveDTO reqDTO, HttpSession session) {

        log.info("게시글 작성 기능 요청 - 제목 : {}", reqDTO.getTitle());

        User sessionUser = (User) session.getAttribute("sessionUser");
        reqDTO.validate();
        boardRepository.save(reqDTO.toEntity(sessionUser));
        return "redirect:/";
    }


    @GetMapping("/")
    public String index(HttpServletRequest request) {

        log.info("메인 페이지 요청");

        List<Board> boardList = boardRepository.findByAll();

        log.info("현재 가지고 온 게시글 개수 : {}", boardList.size());

        request.setAttribute("boardList", boardList);
        return "index";
    }


    @GetMapping("/board/{id}")
    public String detail(@PathVariable(name = "id") Long id, HttpServletRequest request) {

        log.info("게시글 상세 보기 요청 - id : {}", id);

        Board board = boardRepository.findById(id);

        log.info("게시글 상세 보기 조회 완료 - 제목 : {}, 작성자 : {}", board.getTitle(), board.getUser());

        request.setAttribute("board", board);
        return "board/detail";
    }
}

