require "rubygems"
require "choice"
require "etc"

Choice.options do

  header('')
  header('Required arguments:')

  option :to_branch, :required => true do
    short('-t')
    long('--to-branch=TO-BRANCH')
    desc('Branch to fork to')
  end
  
  separator('')
  separator('Optional arguments:')
  
  option :remote_user, :required => false do
    short('-u')
    long('--remote-user=REMOTE-USER')
    default(Etc.getlogin)
    desc('User id to use for remote repository access')
  end
  
  option :from_branch, :required => false do
    short('-f')
    long('--from-branch=FROM-BRANCH')
    default('master')
    desc('Branch to fork from')
  end
  
  option :repository_map, :required => false do
    short('-m')
    long('--map=REPOSITORY-MAP')
    default('~/repository.map')
    desc('The property file containing a mapping from a repository name to a location')
    desc('(defaults to ~/repository.map)')
  end
  
end
