package com.tenco.blog.board;

import com.tenco.blog.user.User;
import lombok.Data;

/**
 * 클라이언트에게서 넘어온 데이터를
 * Object로 변화해서 전달하는 DTO 역할을 담당한다.
 */
public class BoardRequest {

    // 게시글 저장 DTO
    @Data
    public static class SaveDTO {
        private String title;
        private String content;
        // username 제거 : 세션에서 가져올 예정

        // 게시글 조회 및 저장 시, 작성자 정보(user_id)가 필요하기 때문에
        // Board 클래스에 User 객체가 포함되어 있음
        // (User) <-- toEntity() 호출할 때 세션에서 가져와서 넣어주면 됨
        public Board toEntity(User user) {
            return Board.builder()
                    .title(this.title)
                    .user(user)
                    .content(this.content)
                    .build();
        }

        public void validate() {
            if (title == null || title.trim().isEmpty()) {
                throw new IllegalArgumentException("제목은 필수 입니다.");
            }
            if (content == null || content.trim().isEmpty()) {
                throw new IllegalArgumentException("내용은 필수 입니다.");
            }
        }
    }

    // 게시글 수정용 DTO 설계
    @Data
    public static class UpdateDTO {
        private String title;
        private String content;

        // toEntity 메서드 만들지 않음 (더티 체킹 활용)
        // em.find() <-- Board <--  영속화 <-- 상태값을 변경하면 자동 갱신

        // 유효성 검사
        public void validate() {
            if (title == null || title.trim().isEmpty()) {
                throw new IllegalArgumentException("제목은 필수 입니다.");
            }
            if (content == null || content.trim().isEmpty()) {
                throw new IllegalArgumentException("내용은 필수 입니다.");
            }
        }
    }


}
