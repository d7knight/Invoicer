package org.d7knight.invoicer.utilities;

import java.io.Serializable;


public class Product implements Serializable{	
	private static final long serialVersionUID = -8225128421421125809L;
	public static ProductListener listener;
	public static final ProductListener Default = new ProductListener(){

		public String getProductLabel(Product p) {
			return p.type;
		}
		
	};
    public float amount;//price
    public String type;//name
    public String comments;//unit
    
    //for NullString
    public Product(String _type) {
        this.type = _type;
        this.amount=0;
        this.comments = "";
    }
    //for Utilities
    public Product(){
    	this.type = this.comments = "";
    	this.amount=0;
    }

        
    public void set(float _amount, String _type, String _comments){
    	this.amount = _amount;
        this.type = _type;
        if (_comments == null) {
            _comments = "";
        }

        this.comments = _comments;

    }
	
	@Override
	public boolean equals(Object o){//for step3_preview
		return this.type.equals(((Product)o).type);
	}
	@Override
	public String toString(){//for adapter
		return listener.getProductLabel(this);
	}
    
}
