package com.zygoo13.common.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@ToString(exclude = "users") // tránh vòng lặp vô hạn khi in
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;

    @Column(length = 40, nullable = false, unique = true)
    @NotBlank(message = "Name cannot be blank")
    @Size(max = 40, message = "Name cannot exceed 40 characters")
    private String name;

    @Column(length = 150, nullable = false)
    @NotBlank(message = "Description cannot be blank")
    @Size(max = 150, message = "Description cannot exceed 150 characters")
    private String description;

    @ManyToMany(mappedBy = "roles") // quan hệ ngược lại từ User
    private Set<User> users = new HashSet<>();

    public Role(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public Role(Integer id) {
        this.id = id;
    }


    @Override
    public String toString() {
        return this.name;
    }
}
