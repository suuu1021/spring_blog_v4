package com.tenco.blog.board;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor // 생성자 자동 생성 + 멤버 변수 확인 -> DI 처리
@Repository // IoC 대상 + 싱글톤 패턴 관리 = 스프링 컨테이너
public class BoardRepository {

    // DI
    private final EntityManager em;

    // 게시글 수정하기
    @Transactional
    public Board updateById (Long id, BoardRequest.UpdateDTO reqDTO) {
        // 1. 수정할 게시글을 영속 상태로 조회
        Board board = findById(id);
        board.setTitle(reqDTO.getTitle());
        board.setContent(reqDTO.getContent());
        // dirty checking 동작 과정
        // 1. 영속성 컨텍스트가 엔티티 최초 조회 상태를 스냅샷으로 보관
        // 2. 필드 값 변경 시 현재 상태와 스냅샷 비교
        // 3. 트랜잭션 커밋 시점에 **변경된 필드만 UPDATE 쿼리 자동 생성**

        return board;
    }

    // 게시글 삭제
    @Transactional
    public void deleteById(Long id) {
        // 1. 네이티브 쿼리 (테이블 대상으로 질의어)
        // 2. JPQL(객체 지향 쿼리 언어 - 엔티티 객체를 대상으로 질의어)
        // 3. 영속성 처리 (em) - CRUD
        String jpql = "DELETE FROM Board b WHERE b.id = :id ";
        Query query = em.createQuery(jpql);
        query.setParameter("id", id);
        int deleteCount = query.executeUpdate(); // I, U, D
        if (deleteCount == 0) {
            throw new IllegalArgumentException("삭제할 게시글이 없습니다.");
        }
    }

    @Transactional
    public void deleteByIdSafely(Long id) {
        // 영속성 컨텍스트를 활용한 삭제 처리
        // 1. 먼저 삭제할 엔티티를 영속 상태로 조회
        Board board = em.find(Board.class, id);
        // board -> 영속화 됨
        // 2. 엔티티 존재 여부 확인
        if (board == null) {
            throw new IllegalArgumentException("삭제할 게시글이 없습니다.");
        }
        // 3. 영속화 상태의 엔티티를 삭제 상태로 변경
        em.remove(board);
        // ( 장점 )
        // 1차 캐시에서 자동 제거 된다
        // 연관관계 처리도 자동 수행 된다 (cascade)
    }

    /**
     * 게시글 저장 : User와 연관관계를 가진 Board 엔티티 영속화
     *
     * @param board
     * @return
     */
    @Transactional
    public Board save(Board board) {
        // 비영속 상태의 Board Object를 영속성 컨텍스트에 저장하면
        em.persist(board);
        // 이후 시점에는 사실 같은 메모리 주소를 가리킨다.
        return board;
    }

    /**
     * 전체 게시글 조회
     */
    public List<Board> findByAll() {
        // 조회 - JPQL 쿼리 선택
        String jpql = "SELECT b FROM Board b ORDER BY b.id DESC ";
        TypedQuery query = em.createQuery(jpql, Board.class);
        List<Board> boardList = query.getResultList();
        return boardList;
    }

    /**
     * 게시글 단건 조회 (PK 기준)
     *
     * @param id : Board 엔티티의 ID 값
     * @return : Board 엔티티
     */
    public Board findById(Long id) {
        // 조회 - PK 조회는 무조건 엔티티 매니저의 메서드 활용이 이득이다.
        Board board = em.find(Board.class, id);
        return board;
    }
}
