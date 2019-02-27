package cc.dodder.common.entity;

import lombok.Getter;
import lombok.Setter;
import org.apache.http.HttpStatus;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Getter @Setter
public class Result implements Serializable {

	private int status;
	private String msg;
	private Map extra;

	public Result(int status, String msg) {
		this.status = status;
		this.msg = msg;
	}

	public Result(int status) {
		this.status = status;
	}

	public static Result ok() {
		return new Result(HttpStatus.SC_OK);
	}

	public static Result noContent() {
		return new Result(HttpStatus.SC_NO_CONTENT);
	}

	public static Result notFount() {
		return new Result(HttpStatus.SC_NOT_FOUND);
	}

	public static Result ok(String msg) {
		return new Result(HttpStatus.SC_OK, msg);
	}

	public Result put(String key, Object value) {
		if (extra == null) {
			synchronized (extra) {
				extra = new HashMap();
			}
		}
		extra.put(key, value);
		return this;
	}
}
