package cc.dodder.web.dialect;

import org.springframework.stereotype.Component;
import org.thymeleaf.dialect.AbstractDialect;
import org.thymeleaf.dialect.IExpressionObjectDialect;
import org.thymeleaf.expression.IExpressionObjectFactory;

/***
 * 自定义 Thymeleaf 工具 Dialect
 *
 * @author Mr.Xu
 * @date 2019-03-02 22:35
 **/
@Component
public class GlobalUtilDialect extends AbstractDialect implements IExpressionObjectDialect {

	private final IExpressionObjectFactory GLOBAL_UTIL_EXPRESSION_OBJECTS_FACTORY = new GlobalUtilExpressionObjectFactory();

	protected GlobalUtilDialect() {
		super("dodderUtil");
	}

	@Override
	public IExpressionObjectFactory getExpressionObjectFactory() {
		return GLOBAL_UTIL_EXPRESSION_OBJECTS_FACTORY;
	}
}
