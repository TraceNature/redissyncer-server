package syncer.syncerjedis.commands;

import syncer.syncerjedis.Module;

import java.util.List;

public interface ModuleCommands {
  String moduleLoad(String path);
  String moduleUnload(String name);
  List<Module> moduleList();
}
