#!/usr/bin/env ruby -wKU
$LOAD_PATH << File.expand_path(File.dirname(__FILE__))
$LOAD_PATH << File.expand_path(File.dirname(__FILE__) + '/../lib')

require 'repository'
require "rubygems"
require "etc"

require 'options'

args = Choice.choices

if File.exist?(File.expand_path(args[:repository_map]))
  paths = Hash.new
  IO.foreach(File.expand_path(args[:repository_map])) do |line|
    paths[$1.strip] = $2.strip if line =~ /([^=]*)=(.*)/
  end
else
  paths = {
      'eclipse-mirror' => 'eclipse-mirror',
      'util' => 'util',
      'test' => 'test',
      'medic' => 'medic',
      'nano' => 'nano',
      'artifact-repository' => 'artifact-repository',
      'kernel' => 'kernel',
      'kernel-tools' => 'kernel-tools',
      'web' => 'web',
      'snaps' => 'snaps',
      'apps' => 'apps',
      'documentation' => 'documentation',
      'packaging' => 'packaging'  
  }
end

update_branch = args[:from_branch]

local_repo_root = 'git@git.springsource.org:virgo/'
virgo_repo_root = 'ssh://' + args[:remote_user] + '@git.eclipse.org/gitroot/virgo/org.eclipse.virgo.'

ALL_REPOS = [
  Repository.new(virgo_repo_root, 'packaging',                             paths['packaging'],                       nil, nil, update_branch),
  Repository.new(virgo_repo_root, 'documentation',                         paths['documentation'],                    nil, nil, update_branch),
  Repository.new(virgo_repo_root, 'apps',                                  paths['apps'],                             nil, nil, update_branch),
  Repository.new(virgo_repo_root, 'snaps',                                 paths['snaps'],                            nil, nil, update_branch),
  Repository.new(virgo_repo_root, 'web',                                   paths['web'],                              nil, nil, update_branch),
  Repository.new(virgo_repo_root, 'kernel-tools',                          paths['kernel-tools'],                     nil, nil, update_branch),
  Repository.new(virgo_repo_root, 'kernel',                                paths['kernel'],                           nil, nil, update_branch),
  Repository.new(virgo_repo_root, 'nano',                                  paths['nano'],                             nil, nil, update_branch),
  Repository.new(virgo_repo_root, 'artifact-repository',                   paths['artifact-repository'],              nil, nil, update_branch),
  Repository.new(virgo_repo_root, 'medic',                                 paths['medic'],                            nil, nil, update_branch),
  Repository.new(virgo_repo_root, 'test',                                  paths['test'],                             nil, nil, update_branch),
  Repository.new(virgo_repo_root, 'util',                                  paths['util'],                             nil, nil, update_branch),
  Repository.new(virgo_repo_root, 'eclipse-mirror',                        paths['eclipse-mirror'],                   nil, nil, update_branch)
]


to_branch = args[:to_branch]

start_time = Time.new

ALL_REPOS.each do |repo|
  puts 'Repo: ' + repo.name
  puts '  Checkout with "' + repo.clone_command + '"' 
  repo.checkout(true)
  repo.fork(to_branch)
  puts ''
end

puts 'Execution Time: ' + Time.at(Time.new - start_time).utc.strftime('%R:%S')

print 'Do you want to push? (y/n) '
commit_ok = STDIN.gets.chomp
if commit_ok =~ /y.*/
  ALL_REPOS.each do |repo|
    begin
      repo.push_fork(to_branch)
      puts ''
    rescue SystemExit
      if VIRGO_PERMISSION_REPOS.include?(repo)
        abort("\nFATAL: Push failed for #{repo.name}")
      end
      puts "\nWARNING: Push failed for #{repo.name}"
    end
  end
end


