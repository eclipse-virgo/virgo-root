require "rubygems"
require "choice"
require "etc"

Choice.options do

  header('')
  header('Required arguments:')

  option :new_version, :required => true do
    short('-n')
    long('--new-version=NEW-VERSION')
    desc('New version to set')
  end
  
  separator('')
  separator('Optional arguments:')
  
  option :remote_user, :required => false do
    short('-u')
    long('--remote-user=REMOTE-USER')
    default(Etc.getlogin)
    desc('User id to use for remote repository access')
  end
  
  option :branch, :required => false do
    short('-f')
    long('--branch=BRANCH')
    default('master')
    desc('Branch to update')
  end
  
  option :repository_map, :required => false do
    short('-m')
    long('--map=REPOSITORY-MAP')
    default('~/repository.map')
    desc('The property file containing a mapping from a repository name to a location')
    desc('(defaults to ~/repository.map)')
  end
  
end
