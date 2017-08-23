$LOAD_PATH << File.expand_path(File.dirname(__FILE__))

require 'version'
require 'fileutils'

class Repository

  attr_reader :name
  attr_reader :targets
  attr_reader :clone_command
  attr_reader :bundle_version
  attr_reader :path
  attr_reader :master_branch

  def initialize(repo_root, name, path, variable, bundle_version = nil, master_branch = 'master', targets = 'clean clean-integration test publish-ivy')
    if repo_root.nil?
      abort('Repository Git Root cannot be nil for repository ' + @name)
    end
    @repo_root = repo_root
      
    if name.nil?
      abort('Name cannot be nil')
    end
    @name = name

    if path.nil? || path == ''
      abort('Repository path cannot be nil for repository ' + @name)
    end
    @path = File.expand_path(path)

    @variable = variable

    @bundle_version = bundle_version

    if targets.nil?
      abort('Repository build targets cannot be nil for repository ' + @name)
    end
    @targets = targets

    if master_branch.nil?
      abort('Repository master branch cannot be nil for repository ' + @name)
    end
    @master_branch = master_branch
    
    @clone_command = 'git clone -b ' + @master_branch + " " + @repo_root + @name + '.git ' + @path 
  end

  def checkout(quietly=false)
    if File.exist?(@path)
      puts '  Deleting old checkout at ' + @path
      FileUtils.rm_rf(@path)
    end
    tonull = " > /dev/null 2>&1"
    puts '  Checking out ' + @path
    execute(@clone_command + (quietly ? tonull : ""))
    Dir.chdir(@path)
    execute('git submodule update --init' + (quietly ? tonull : ""))
    if @bundle_version.nil? 
      create_new_bundle_version_from_properties
    end
  end

  def create_release_branch(version, build_stamp, release_type, versions)
    create_branch(@bundle_version)
    update_build_properties(version, build_stamp, release_type)
    update_build_versions(versions)
  end

  def update_versions(versions)
    create_branch(@bundle_version)
    puts '  Updating versions'
    versions.sort.reverse.each do |var_version|
      Version.update(var_version[0], var_version[1], @path, true)
    end
    execute('cd ' + @path + '; git commit --allow-empty -a -m "[RIPPLOR] Updated versions"')
  end

  def build(committerId, log_file)
    puts '  Building:'
    puts '    BUNDLE_VERSION: ' + @bundle_version
    puts '    TARGETS: ' + @targets
    
    execute('ant -f ' + @path + '/build-*/build.xml -Dvirgo.deps.location=integration-repo -Declipse.committerId=' + committerId + ' -Dbundle.version=' + @bundle_version + ' ' + @targets + ' >> ' + log_file)
  end

  def create_tag
    puts '    Creating tag ' + @bundle_version
    Dir.chdir(@path)

    puts "hello"+"world"
    execute('git tag -a -m "[RELEASELOR] ' + @bundle_version + '" ' + @bundle_version)
  end

  def update_master_branch(new_version, versions)
    create_branch(new_version)
    update_build_properties(new_version)
    update_build_versions(versions)
  end

  def versions
    versions = Hash.new
    
    IO.foreach(@path + '/build.versions') do |line|
      if line =~ /([^=]*)=(.*)/
        if !($1.strip[-6..-1] == '-RANGE')
          versions[$1.strip] = $2.strip 
        end
      end
    end
  
    versions[@variable] = @bundle_version
    versions
  end

  def push(new_version=nil)
    new_version = @bundle_version if new_version.nil?
    puts 'Pushing ' + @name
    Dir.chdir(@path)
    execute('git push origin ' + new_version + ':' + @master_branch + ' --tags')
  end
  
  def update_virgo_build(new_version)
    puts '  Updating to Virgo Build version \'' + new_version + '\''
    Dir.chdir(@path + "/virgo-build")
    execute("git pull origin master:master ")
    execute("git fetch --tags")
    execute("git checkout " + new_version)
    Dir.chdir(@path)
    execute('git commit --allow-empty -a -m "[UPDATE BUILDLOR] Updated Virgo Build to \'' + new_version + '\'"')
  end
  
  def fork(branch_name)
    create_branch(branch_name)
  end
  
  def push_fork(branch_name)
    puts 'Pushing ' + @name
    Dir.chdir(@path)
    execute('git push origin ' + branch_name)
  end
  
  def update_version(new_version)
    create_branch(new_version)
    update_build_properties(new_version, nil, 'integration', 'VERSIONOR')
  end

########################################################################################################################

  private

  def create_branch(name)
    puts('  Creating branch ' + name + ' -> ' + @master_branch)
    execute('cd ' + @path + '; git checkout -q -b ' + name + ' --track origin/' + @master_branch)
  end
  
  def create_new_bundle_version_from_properties
    version = nil
    IO.foreach(@path + '/build.properties') do |line|
      version = $1.strip if line =~ /^version=(.*)/
    end

    @bundle_version = version + '.D-' + Time.now.utc.strftime("%Y%m%d%H%M%S")
  end

  def update_build_properties(version, build_stamp = nil, release_type = 'integration', name = 'RELEASELOR')
    properties = @path + '/build.properties'
    puts '    Updating properties'
    lines = IO.readlines(properties)
    lines.each do |line|
      if line =~ /^version/
        line.gsub!(/^version.*/, 'version=' + version)
        
        if(!build_stamp.nil?)
          lines.insert(lines.index(line) + 2, 'build.stamp=' + build_stamp + $/)
        end
      elsif line =~ /^release\.type/
        line.gsub!(/^release\.type.*/, 'release.type=' + release_type)
      end
    end
    write_file(properties, lines)
    execute('cd ' + @path + '; git commit --allow-empty -a -m "[' + name + '] Updated properties"')
  end

  def update_build_versions(versions)
    puts '    Updating versions'
    versions.sort.reverse.each do |var_version|
      Version.update(var_version[0], var_version[1], @path, true)
    end
    execute('cd ' + @path + '; git commit --allow-empty -a -m "[RELEASELOR] Updated versions"')
  end

  def execute(command)
    output = `#{command}`
    if $?.to_i != 0
      abort('Execution Failed')
    end
    output
  end

  def write_file(path, lines)
    file = File.new(path, 'w')
    lines.each do |line|
      file.write(line)
    end
    file.close
  end

end