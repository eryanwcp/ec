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
package com.bstek.ureport.parser.impl.searchform;

import com.bstek.ureport.definition.searchform.Component;
import com.bstek.ureport.definition.searchform.FormPosition;
import com.bstek.ureport.definition.searchform.SearchForm;
import com.bstek.ureport.parser.Parser;
import org.dom4j.Element;

import java.util.List;

/**
 * @author Jacky.gao
 * @since 2017年10月24日
 */
public class SearchFormParser implements Parser<SearchForm> {
	@Override
	public SearchForm parse(Element element) {
		SearchForm form=new SearchForm();
		form.setFormPosition(FormPosition.valueOf(element.attributeValue("form-position")));

		//解析加载 表单增强JS/CSS
		for(Object obj:element.elements()){
			if(obj==null || !(obj instanceof Element)){
				continue;
			}
			Element ele=(Element)obj;
			if(ele.getName().equals("js-data")){
				String loadJs =  ele.getText().trim();
				form.setLoadJs(loadJs);
			}
			if(ele.getName().equals("css-data")){
				String loadCss =  ele.getText().trim();
				form.setLoadCss(loadCss);
			}
			if(ele.getName().equals("title-data")){
				String fromTitle =  ele.getText().trim();
				form.setFromTitle(fromTitle);
			}
		}

		List<Component> components=FormParserUtils.parse(element);
		form.setComponents(components);
		return form;
	}
}
