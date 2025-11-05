/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author D E L L
 */
public class BankDTO {
    private int id;
    private String name;
    private String code;
    private String bin;
    private String shortName;
    private String logo;

    public BankDTO() {
    }

    public BankDTO(int id, String name, String code, String bin, String shortName, String logo) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.bin = bin;
        this.shortName = shortName;
        this.logo = logo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getBin() {
        return bin;
    }

    public void setBin(String bin) {
        this.bin = bin;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    @Override
    public String toString() {
        return "BankDTO{" + "id=" + id + ", name=" + name + ", code=" + code + ", bin=" + bin + ", shortName=" + shortName + ", logo=" + logo + '}';
    }
    
    
}
