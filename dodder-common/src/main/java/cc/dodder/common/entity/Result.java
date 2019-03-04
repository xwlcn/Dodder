package cc.dodder.common.entity;

import lombok.Getter;
import lombok.Setter;
import org.apache.http.HttpStatus;

import java.io.Serializable;

@Getter @Setter
public class Result<T> implements Serializable {

	private int status;
	private String msg;
	private T data;

	public Result(int status) {
		this.status = status;
	}

	private Result(int status, String msg, T data) {
		this.status = status;
		this.msg = msg;
		this.data = data;
	}

	public static Result ok() {
		return ok(null);
	}

	public static Result ok(String msg) {
		return ok(msg, null);
	}

	public static <T>Result<T> ok(T data) {
		return ok(null, data);
	}

	public static <T>Result<T> ok(String msg, T data) {
		return new Result<>(HttpStatus.SC_OK, msg, data);
	}

	public static Result noContent() {
		return new Result(HttpStatus.SC_NO_CONTENT);
	}

	public static Result notFount() {
		return new Result(HttpStatus.SC_NOT_FOUND);
	}

}
