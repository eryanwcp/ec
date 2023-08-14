package com.eryansky.codegen.generate;

import com.eryansky.codegen.vo.Table;

import java.util.List;

/**
 * 根据模板生成文件
 */
public interface Generate {

	void generate(Table table) throws Exception;

	void generate(List<Table> tables) throws Exception;

}
