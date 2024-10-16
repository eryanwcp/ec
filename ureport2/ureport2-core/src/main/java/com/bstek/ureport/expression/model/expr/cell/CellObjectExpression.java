/*******************************************************************************
 * Copyright 2017 Bstek
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.bstek.ureport.expression.model.expr.cell;

import org.apache.commons.lang3.StringUtils;

import com.bstek.ureport.Utils;
import com.bstek.ureport.build.Context;
import com.bstek.ureport.expression.model.data.ExpressionData;
import com.bstek.ureport.expression.model.data.ObjectExpressionData;
import com.bstek.ureport.expression.model.expr.BaseExpression;
import com.bstek.ureport.model.Cell;

/**
 * @author Jacky.gao
 * @since 2017年1月20日
 */
public class CellObjectExpression extends BaseExpression {
	private static final long serialVersionUID = 1558531964770533126L;
	private String property;
	public CellObjectExpression(String property) {
		this.property=property;
	}
	@Override
	protected ExpressionData<?> compute(Cell cell, Cell currentCell,Context context) {
		while(!context.isCellPocessed(cell.getName())){
			context.getReportBuilder().buildCell(context, null);
		}
		if(StringUtils.isNotBlank(property)){
			Object obj=Utils.getProperty(cell, property);
			return new ObjectExpressionData(obj);
		}
		return new ObjectExpressionData(cell);
	}
}
