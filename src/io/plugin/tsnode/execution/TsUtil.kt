package io.plugin.tsnode.execution

import com.intellij.execution.ProgramRunnerUtil
import com.intellij.execution.RunManager
import com.intellij.execution.configurations.RuntimeConfigurationException
import com.intellij.execution.executors.DefaultDebugExecutor
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.impl.RunnerAndConfigurationSettingsImpl
import com.intellij.javascript.nodejs.util.NodePackage
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataKeys
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VirtualFile
import io.plugin.tsnode.lib.TsData
import io.plugin.tsnode.lib.TsLog
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

object TsUtil
{
	public val TypeScriptFileType = TsData.FileTypeClassName

	val LOG = TsLog(javaClass)

	val logger2 = Logger.getInstance(javaClass)

	private val configurations = HashMap<String, RunnerAndConfigurationSettingsImpl>()

	fun isTypeScript(virtualFile: VirtualFile): Boolean
	{
		return TypeScriptFileType == virtualFile.fileType.javaClass.name
	}

	fun compatibleFiles(event: AnActionEvent): List<VirtualFile>
	{
		val files = event.getData(DataKeys.VIRTUAL_FILE_ARRAY).orEmpty()

		logger2.debug("[tsnode][compatibleFiles]" + files.size)

		return files
			.filter { it -> TsUtil.isTypeScript(it) }
	}

	fun executable(project: Project, virtualFile: VirtualFile): Boolean
	{
		TsLog.debug("virtualFile.fileType.name=" + virtualFile.fileType.name)
		TsLog.debug(virtualFile.fileType.defaultExtension)
		TsLog.debug(virtualFile.fileType.javaClass.name)

		return TypeScriptFileType == virtualFile.fileType.name
			//&& getConfiguration(project, virtualFile) != null
	}

	private fun getConfiguration(project: Project, virtualFile: VirtualFile): RunnerAndConfigurationSettingsImpl?
	{
		val tsPath = virtualFile.canonicalPath
		val configuration: RunnerAndConfigurationSettingsImpl? = configurations[tsPath]

		if (configuration == null)
		{
			if (tsPath == null) return null

			if (virtualFile.exists())
			{
				val runManager = RunManager.getInstance(project)

				//runManager.addConfiguration(configuration)
			}

		}

		return configuration
	}

	fun execute(project: Project, virtualFile: VirtualFile, debug: Boolean)
	{
		val runManager = RunManager.getInstance(project)
		val configuration = getConfiguration(project, virtualFile)

		logger2.debug("[tsnode][execute]" + configuration)

		if (configuration != null)
		{
			val configurationsList = runManager.getConfigurationsList(TsConfigurationType.getInstance())
			if (!configurationsList.contains(configuration.configuration))
			{
				runManager.addConfiguration(configuration)
			}
			if (runManager.selectedConfiguration !== configuration)
			{
				runManager.selectedConfiguration = configuration
			}
			ProgramRunnerUtil.executeConfiguration(configuration,
				if (debug) DefaultDebugExecutor.getDebugExecutorInstance() else DefaultRunExecutor.getRunExecutorInstance())
		}
	}

	@Throws(RuntimeConfigurationException::class)
	fun expectFile(path: String, throwError: Boolean = false, name: String = "path"): Boolean
	{
		if (StringUtil.isEmptyOrSpaces(path))
		{
			if (throwError)
			{
				throw RuntimeConfigurationException("No $name given.")
			}

			return false
		}

		val file = File(path)

		if (!file.isFile || !file.canRead())
		{
			if (throwError)
			{
				throw RuntimeConfigurationException("$name is invalid or not readable.")
			}

			return false
		}

		return true
	}

	fun NodePackagePathResolve(pkg: NodePackage, path: String): Path
	{
		return Paths.get(pkg.systemDependentPath)
			.resolve(path)
	}

	fun tsnodePath(runConfig: TsRunConfiguration): String
	{
		// 改進 搜尋 ts-node bin 的方法
		var file = runConfig.selectedTsNodePackage().findBinFile()!!.absoluteFile.toPath().toAbsolutePath();

		if (file == null)
		{
			file = TsUtil.NodePackagePathResolve(runConfig.selectedTsNodePackage(), """dist${File.separatorChar}bin.js""").toAbsolutePath();
		}

		//LOG.info("""[tsnodePath] ${file}""");

		return file.toString()
	}
}
