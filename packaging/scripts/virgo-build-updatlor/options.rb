require "rubygems"
require "choice"
require "etc"

Choice.options do

  header('')
  header('Required arguments:')

  option :build_version, :required => true do
    short('-b')
    long('--virgo-build-version=VIRGO-BUILD-VERSION')
    validate(/\d(.\d(.\d(.([\w_-])+)?)?)?/)
    desc('The version to update Virgo Build to')
  end
  
  separator('')
  separator('Optional arguments:')
  
  option :remote_user, :required => false do
    short('-u')
    long('--remote-user=REMOTE-USER')
    default(Etc.getlogin)
    desc('User id to use for remote repository access')
  end
  
  option :repository_map, :required => false do
    short('-m')
    long('--map=REPOSITORY-MAP')
    default('~/repository.map')
    desc('The property file containing a mapping from a repository name to a location')
    desc('(defaults to ~/repository.map)')
  end
  
  option :branch_name, :required => false do
   short('-c')
   long('--branch=BRANCH-NAME')
   default('master')
   desc('The branch to be updated')
   desc('(defaults to master)')
  end

  option :gemini_branch_name, :required => false do
   short('-d')
   long('--gemini-branch=BRANCH-NAME')
   default('master')
   desc('The Gemini Web branch to be updated')
   desc('(defaults to master)')
  end
   
end
