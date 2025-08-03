package com.paras.Arthra.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="tbl_profiles")
@Data // Lombok annotation to generate getters, setters, toString, equals, and hashCode
@AllArgsConstructor // Lombok annotation to generate a constructor with all fields as parameters
@NoArgsConstructor // Lombok annotation to generate a no-argument constructor (required by JPA)
@Builder // Lombok annotation to implement the builder pattern for this class
public class ProfileEntity {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    private String fullName;

    @Column(unique=true)
    private String email;

    private String password;

    private String profileImageUrl;

    @Column(updatable=false) //Indicates that this database column value cannot be modified once the entity is initially persisted.
    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private Boolean isActive;

    private String activationToken;

    
    
    //This method is executed before the entity is persisted (saved) for the first time.
    @PrePersist
    public void prePersist(){
        if(this.isActive==null){
            isActive=false;
        }
    }

}
