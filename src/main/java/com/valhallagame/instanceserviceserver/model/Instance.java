package com.valhallagame.instanceserviceserver.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name = "instance")
public class Instance {

    @Id
    @Column(unique = true, name = "instance_name")
    private String instanceName;

    @Column()
    private String owner;
    
    @Column(unique = true, name = "display_instance_name")
    private String displayInstanceName;

    @Column(name = "chest_item")
    private String chestItem;
    
    @Column(name = "mainhand_armament")
    private String mainhandArmament;
    
    @Column(name = "off_hand_armament")
    private String offHandArmament;
    
}
