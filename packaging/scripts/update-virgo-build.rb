#!/usr/bin/env ruby -wKU
$LOAD_PATH << File.expand_path(File.dirname(__FILE__))

require "rubygems"
require "choice"
require "etc"

Choice.options do

  header('')
  header('Required arguments:')

  option :new_version, :required => true do
    short('-n')
    long('--new-version=NEW-VERSION')
    validate(/\d(.\d(.\d(.([\w_-])+)?)?)?/)
    desc('The version to update to')
  end
  
end

puts 'Updating to Virgo-Build version \'' + Choice.choices[:new_version] + '\''

def execute(command)
  output = `#{command}`
  if $?.to_i != 0
    abort('Execution Failed, aborted.')
  end
  output
end

def do_update(path, new_version)
  puts '  Updating to Virgo Build version \'' + new_version + '\''
  Dir.chdir(path)
  execute("git submodule update --init")
  Dir.chdir(path + "/virgo-build")
  execute("git pull origin master:master ")
  execute("git fetch --tags")
  execute("git checkout " + new_version)
  Dir.chdir(path)
  execute('git commit --allow-empty -a -m "[UPDATE BUILDLOR] Updated Virgo Build to \'' + new_version + '\'"')
end

do_update(Dir.pwd, Choice.choices[:new_version])

