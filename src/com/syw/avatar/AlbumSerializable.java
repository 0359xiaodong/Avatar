package com.syw.avatar;

import java.io.Serializable;
import java.util.List;

/**    
 */
public class AlbumSerializable implements Serializable {

	/** 
	 * @fields serialVersionUID 
	 */ 
	
	private static final long serialVersionUID = 1L;
	
	private List<AlbumInfo> list;

	public List<AlbumInfo> getList() {
		return list;
	}

	public void setList(List<AlbumInfo> list) {
		this.list = list;
	}

}
