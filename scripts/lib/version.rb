require 'find'

class Version

  @@SEARCH_EXTENSIONS = [
    'classpath',
    'iml',
    'java',
    'properties',
    'versions',
    'xml'
  ]

  def self.update(variable, version, root, verbose=false)  
    existing_version = nil
    IO.foreach(root + '/build.versions') do |line|
      existing_version = $1.strip if line !~ /^\s*#/ && line =~ /#{variable}=(.*)/
    end
    
    if existing_version.nil?
      puts '      No variable <' + variable + '> found in ./build.versions file.'
    else
      if existing_version != version
        puts '      Updating <' + variable + '> from "' + existing_version + '" to "' + version + '"'
        puts '       (-------: files in which <' + variable + '> is found with a version string are listed, those updated so prefixed)' if verbose
        changesMade = false
        Find.find(root) do |path|
          Find.prune if dirAtPath?(path,["ivy-cache","target","integration-repo","virgo-build"])
          if FileTest.file?(path) && @@SEARCH_EXTENSIONS.include?(get_extension(path))
            lines = IO.readlines(path)
            changed = false
            encountered = false
            lines.each do |line|
              if line =~ /#{variable}[^0-9A-Za-z]/
                encountered = true if line =~ /([0-9]+\.[0-9]+(\.[0-9]+(\.[0-9A-Za-z_\-]+)?)?)/
                changed = true if line.gsub!(/#{existing_version}/, version)
              end
            end
      
            if changed
              puts '        updated: ' + path if verbose
              write_file(path, lines)
              changesMade = true
            else
              puts '        -------: ' + path if verbose && encountered
            end
          end
        end
        puts '        No files found using <' + variable + '> with version "' + existing_version + '"' if verbose && !changesMade
      else
        puts '      Variable <' + variable + '> not updated, version already set as "' + version + '"'
      end
    end
  end

########################################################################################################################

  private

  def self.dirAtPath?(path, dirs)
    dirs.include?(path.split("/")[-1])
  end
  
  def self.get_extension(path)
    match_data = path.match('/.*\.(.*)')
    if match_data.nil?
      nil
    else
      match_data[1]
    end
  end
  
  def self.write_file(path, lines)
    file = File.new(path, 'w')
    lines.each do |line|
      file.write(line)
    end
    file.close
  end
  
end