package com.youfeng.sfs.mobiletools.di

import com.youfeng.sfs.mobiletools.data.repository.AssetsRepository
import com.youfeng.sfs.mobiletools.data.repository.AssetsRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt 模块，用于提供仓库层的依赖关系
 */
@Suppress("UNUSED_PARAMETER")
@Module
@InstallIn(SingletonComponent::class) // 安装在 Application 级别的组件中
abstract class RepositoryModule {

    @Binds
    abstract fun bindAssetsRepository(
        assetsRepositoryImpl: AssetsRepositoryImpl
    ): AssetsRepository

}