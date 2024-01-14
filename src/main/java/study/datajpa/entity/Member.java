package study.datajpa.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter @Setter
@ToString(of = {"id", "username", "age"}) // Team 은 무한루프 유발할 수 있음
public class Member {

    @Id
    @GeneratedValue
    @Column(name = "member_id")
    private Long id;
    private String username;
    private int age;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    // JPA가 proxy 기술을 쓰는데 JPA구현체들이 (하이버네이터 등..)
    // 막 proxing 하고 막 객체를 강제로 어떻게 만들어내야 되는데
    // 그때 기본 생성자가 private 로 되어있으면 막힐 수가 있다.
    // 롬복으로도 가능
    protected Member() {
    }

    public Member(String username) {
        this.username = username;
    }

    public Member(String username, int age) {
        this(username, age, null);
    }
    public Member(String username, int age, Team team) {
        this.username = username;
        this.age = age;
        if (team != null) {
            changeTeam(team);
        }
    }
    public void changeTeam(Team team) {
        this.team = team;
        team.getMembers().add(this);
    }
}
