package com.syw.avatar;

import java.io.Serializable;
import java.util.List;

/**    
 */
public class PhotoSerializable implements Serializable {

	private List<PhotoInfo> list;

	
	public PhotoSerializable() {
    }

    public PhotoSerializable(List<PhotoInfo> list) {
        this.list = list;
    }

    public List<PhotoInfo> getList() {
		return list;
	}

	public void setList(List<PhotoInfo> list) {
		this.list = list;
	}
	
}
