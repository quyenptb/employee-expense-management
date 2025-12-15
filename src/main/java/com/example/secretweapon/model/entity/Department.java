package com.example.secretweapon.model.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Department {
   //id, name, parent_id, manager_id
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   @Column(name = "id") 
   private Long id;

   private String name;

   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "parent_id")
   private Department parent;

   @ManyToOne(fetch = FetchType.LAZY) //owning side
   @JoinColumn(name = "manager_id")
   private User manager; 
    
}
