



package com.example.textcheckbox;

public class Notes {
    
    
    int _uuid;
    String _note;
    String _color;
   
     
    
    public Notes(){
         
    }
   
    public Notes(int id, String name,String color){
        this._uuid = id;
        this._note = name;
        
        this._color=color;
    }
     
    
    public Notes(String name,String color){
        this._note = name;
        this._color=color;
       
    }
   
    public int getID(){
        return this._uuid;
    }
     
  
    public void setID(int id){
        this._uuid = id;
    }
     
    
    public String getNote(){
        return this._note;
    }
     
    
    public void setName(String name){
        this._note = name;
    }
    
    public String getColor(){
        return this._color;
    }
     
    
    public void setColor(String name){
        this._color = name;
    }
     
   
}