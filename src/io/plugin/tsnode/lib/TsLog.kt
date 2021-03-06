package io.plugin.tsnode.lib

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.text.StringUtil

private val DEFAULT_NAME = "#io.plugin.tsnode"

fun TsLog() = Logger.getInstance(DEFAULT_NAME)

fun TsLog(name: Any?): Logger
{
	if (name is Class<*>)
	{
		return Logger.getInstance(name)
	}
	else if (name != null && !StringUtil.isEmptyOrSpaces(name.toString()))
	{
		return Logger.getInstance(name.toString())
	}

	return Logger.getInstance(DEFAULT_NAME)
}

val TsLog = TsLog()
