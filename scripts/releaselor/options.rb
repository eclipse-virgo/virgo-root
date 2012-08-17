require "rubygems"
require "choice"
require "etc"

Choice.options do

  header('')
  header('Required arguments:')

  option :version, :required => true do
    short('-v')
    long('--version=VERSION')
    desc('The version number of the release')
  end

  option :build_stamp, :required => true do
    short('-q')
    long('--build-stamp=BUILD-STAMP')
    desc('The build stamp for the release, e.g. M01, RELEASE')
  end

  option :release_type, :required => true do
    short('-t')
    long('--release-type=RELEASE-TYPE')
    desc('The release type for the release, e.g. milestone, release')
  end

  option :new_version, :required => true do
    short('-n')
    long('--new-version=NEW-VERSION')
    desc('The new version number to be used after the release')
  end

  separator('')
  separator('Optional arguments:')

  option :build_version, :required => false do
    short('-b')
    long('--virgo-build-version=VIRGO-BUILD-VERSION')
    validate(/\d(.\d(.\d(.([\w_-])+)?)?)?/)
    desc('The version to update Virgo Build to')
  end

  option :repository_map, :required => false do
    short('-m')
    long('--map=REPOSITORY-MAP')
    default('~/repository.map')
    desc('The property file containing a mapping from a repository name to a location')
    desc('(defaults to ~/repository.map)')
  end

  option :remote_user, :required => false do
   short('-u')
   long('--remote-user=REMOTE-USER')
   default(Etc.getlogin)
   desc('User id to use for remote repository access')
   desc('(defaults to local login id)')
  end

  option :product_release, :required => false do
   short('-r')
   long('--product-release=PRODUCT')
   desc('The product to be released, kernel, web-server, virgo, full-product')
   desc('(defaults to releasing all the virgo repos)')
  end

  option :branch_name, :required => false do
   short('-c')
   long('--branch=BRANCH-NAME')
   default('master')
   desc('The branch to be released')
   desc('(defaults to master)')
  end

  option :gemini_version, :required => false do
    short('-g')
    long('--gemini-version=GEMINI-BUILD-VERSION')
    desc('When producing a full-product build the Gemini version must be given')
  end

  option :gemini_build_stamp, :required => false do
    short('-h')
    long('--gemini-build-stamp=GEMINI-BUILD-STAMP')
    desc('The build stamp for the Gemini Web release, e.g. M01, RELEASE')
  end
  
  option :gemini_release_type, :required => false do
    short('-j')
    long('--gemini-release-type=GEMINI-RELEASE-TYPE')
    desc('The release type for the Gemini Web release, e.g. milestone, release')
  end

  option :gemini_new_version, :required => false do
    short('-k')
    long('--gemini-new-version=GEMINI-NEW-VERSION')
    desc('The new Gemini Web version number to be used after the release')
  end
  
  option :gemini_branch_name, :required => false do
   short('-d')
   long('--gemini-branch=BRANCH-NAME')
   default('master')
   desc('The Gemini Web branch to be released')
   desc('(defaults to master)')
  end
   
  option :dryrun?, :required => false do
   long('--dry-run')
   desc('Show what would happen but do not actually do anything')
  end

end
