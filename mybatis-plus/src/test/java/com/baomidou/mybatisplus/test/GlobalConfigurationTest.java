/**
 * Copyright (c) 2011-2014, hubin (jobob@qq.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.baomidou.mybatisplus.test;

import com.baomidou.mybatisplus.MybatisSessionFactoryBuilder;
import com.baomidou.mybatisplus.entity.GlobalConfiguration;
import com.baomidou.mybatisplus.mapper.Condition;
import com.baomidou.mybatisplus.test.mysql.NotPKMapper;
import com.baomidou.mybatisplus.test.mysql.TestMapper;
import com.baomidou.mybatisplus.test.mysql.entity.NotPK;
import com.baomidou.mybatisplus.test.mysql.entity.Test;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.Assert;

import java.io.InputStream;
import java.util.Date;
import java.util.UUID;

/**
 * <p>
 * 全局配置测试
 * </p>
 *
 * @author Caratacus
 * @Date 2016-12-22
 */
public class GlobalConfigurationTest {

	/**
	 * 全局配置测试
	 */
	public static void main(String[] args) {

		// 加载配置文件
		InputStream inputStream = GlobalConfigurationTest.class.getClassLoader().getResourceAsStream("mysql-config.xml");
		MybatisSessionFactoryBuilder factoryBuilder = new MybatisSessionFactoryBuilder();
		GlobalConfiguration global = GlobalConfiguration.defaults();
		// 不需要主键依然注入部分通用方法
		global.setInjectionRule(2);
		global.setAutoSetDbType(true);
		// 设置全局校验机制为FieldStrategy.Empty
		global.setFieldStrategy(2);
		factoryBuilder.setGlobalConfig(global);
		SqlSessionFactory sessionFactory = factoryBuilder.build(inputStream);
		SqlSession session = sessionFactory.openSession(false);
		TestMapper testMapper = session.getMapper(TestMapper.class);
		Test test = new Test();
		test.setCreateTime(new Date());
		// 开启全局校验字符串会忽略空字符串
		test.setType("");
		testMapper.insert(test);

		SqlSession sqlSession = sessionFactory.openSession(false);
		NotPKMapper pkMapper = sqlSession.getMapper(NotPKMapper.class);
		NotPK notPK = new NotPK();
		notPK.setUuid(UUID.randomUUID().toString());
		int num = pkMapper.insert(notPK);
		Assert.assertTrue(num > 0);
		NotPK notPK1 = pkMapper.selectOne(notPK);
		Assert.assertNotNull(notPK1);
		pkMapper.selectPage(RowBounds.DEFAULT, Condition.Empty());
		NotPK notPK2 = null;
		try {
			notPK2 = pkMapper.selectById("1");
		} catch (Exception e) {
			System.out.println("因为没有主键,所以没有注入该方法");
		}
		Assert.assertNull(notPK2);
		int count = pkMapper.selectCount(Condition.Empty());
		Assert.assertTrue(count > 0);
		int deleteCount = pkMapper.delete(null);
		Assert.assertTrue(deleteCount > 0);
		session.rollback();
		sqlSession.commit();
	}
}
