package study.datajpa.repository;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    List<Member> findByUsernameAndAgeGreaterThan(String username, int age);

    @Query("select m from Member m where m.username= :username and m.age = :age")
    List<Member> findUser(@Param("username") String username, @Param("age") int age);

    @Query("select m.username from Member m")
    List<String> findUsernameList();

    @Query("select new study.datajpa.dto.MemberDto(m.id, m.username, t.name)" + "from Member m join m.team t")
    List<MemberDto> findMemberDto();

    @Query("select m from Member m where m.username = :name")
    Member findMembers(@Param("name") String username);

    @Query("select m from Member m where m.username in :names")
    List<Member> findByNames(@Param("names") List<String> names);

    List<Member> findListByUsername(String name); //컬렉션
    Member findMemberByUsername(String name); //단건
    Optional<Member> findOptionalByUsername(String name); //단건 Optional

    Page<Member> findByAge(int age, Pageable pageable);

    @Query(value = "select m from Member m",
            countQuery = "select count(m.username) from Member m")
    Page<Member> findMemberAllCountBy(Pageable pageable);

    @Modifying
    @Query("update Member m set m.age = m.age + 1 where m.age >= :age")
    int bulkAgePlus(@Param("age") int age);

    @Query("select m from Member m left join fetch m.team")
    List<Member> findMemberFetchJoin();

    //공통 메서드 오버라이드
    @Override
    @EntityGraph(attributePaths = {"team"})
    List<Member> findAll();

    //JPQL + 엔티티 그래프
    @EntityGraph(attributePaths = {"team"})
//    @EntityGraph("Member.all")
    @Query("select m from Member m")
    List<Member> findMemberEntityGraph();

    //메서드 이름으로 쿼리에서 특히 편리하다.
    @EntityGraph(attributePaths = {"team"})
    List<Member> findEntityGraphByUsername(String username);

    // 성능 장애중 몇퍼센트일 뿐이다. 조회라고 전부 다 넣지는 않고 정말 오래걸리는 몇몇 쿼리들에 한 에서 성능 테스트 해보고 결정해서 넣는다.
    // 조회 성능이 이미 딸리면 애초에 레디스(든 다른 캐시든)를 도입했어야 한다.
    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true")) // 내부적으로 최적화를 해서 스냅샷을 안만든다.
    Member findReadOnlyByUsername(String username);

    // 쿼리 힌트 Page 예제
    @QueryHints(value = { @QueryHint(name = "org.hibernate.readOnly", value = "true")},
            forCounting = true) // true 가 기본값
            Page<Member> findPageHintByUsername(String name, Pageable pageable);

    // 실시간 트래픽이 많은 서비스에서는 가급적이면 lock을 걸면 안된다.
    // 특히 pessimistic lock을 걸어버리면 다 lock 이 걸리기 때문에 걸려면 optimistic lock 이라고 실제 락을 거는 게 아니라 버전링이라는 매커니즘으로 해결하는 방법이 있다.
    // 락을 안 걸고 다른 방법으로 해결하는 게 좋다. 실무에서 락은 최후의 보루 정도로 생각해야한다. 생각해보면 우리 일상의 거의 모든 곳에 락을 걸어야 하는데, 그렇지 않아도 서비스는 잘 동작한다.
    // 영한님 경험 상 락을 풀어두고 발생하는 이슈보다 락을 걸어서 발생하는 이슈가 더 많았다.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Member> findLockByUsername(String name);
}
