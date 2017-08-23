#!/usr/bin/env ruby -wKU
$LOAD_PATH << File.expand_path(File.dirname(__FILE__))
$LOAD_PATH << File.expand_path(File.dirname(__FILE__) + '/../lib')

require 'repository'
require 'options'
require 'console'

args = Choice.choices
SCRIPT_NAME = "releaselor"

bundle_version = args[:version] + '.' + args[:build_stamp]
if args[:product_release] == 'full-product'
  gemini_version = args[:gemini_version] + '.' + args[:gemini_build_stamp]
end
release_from_branch = args[:branch_name]
gemini_release_from_branch = args[:gemini_branch_name]

DRY_RUN = args[:dryrun?].nil? ? false : true
puts "This is a dry run..." if DRY_RUN

if File.exist?(File.expand_path(args[:repository_map]))
  paths = Hash.new
  IO.foreach(File.expand_path(args[:repository_map])) do |line|
	paths[$1.strip] = $2.strip if line =~ /([^=]*)=(.*)/
  end
else
    paths = {
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
        'packaging' => 'packaging',
        'gemini-web' => 'gemini-web' 
    }
end

virgo_eclipse_repo_root = 'ssh://' + args[:remote_user] + '@git.eclipse.org/gitroot/virgo/org.eclipse.virgo.'
gemini_eclipse_repo_root = 'ssh://' + args[:remote_user] + '@git.eclipse.org/gitroot/gemini.web/org.eclipse.gemini.web.'
default_targets = 'clean clean-integration test publish-ivy publish-maven'
clean_all_targets = 'clean clean-all-integration clean-ivy test publish-ivy publish-maven'


if args[:product_release] == 'full-product'

  ALL_REPOS = [
	Repository.new(virgo_eclipse_repo_root,	 'util',					paths['util'],					'org.eclipse.virgo.util',			bundle_version, release_from_branch,		clean_all_targets),
	Repository.new(virgo_eclipse_repo_root,	 'test',					paths['test'],					'org.eclipse.virgo.test',			bundle_version, release_from_branch,		default_targets),
	Repository.new(virgo_eclipse_repo_root,	 'medic',					paths['medic'],					'org.eclipse.virgo.medic',			bundle_version, release_from_branch,		default_targets),
	Repository.new(virgo_eclipse_repo_root,	 'nano',					paths['nano'],					'org.eclipse.virgo.nano',			bundle_version, release_from_branch,		'clean clean-integration test publish-ivy publish-maven'),
	Repository.new(virgo_eclipse_repo_root,	 'artifact-repository',		paths['artifact-repository'],	'org.eclipse.virgo.repository',		bundle_version, release_from_branch,		default_targets),
	Repository.new(virgo_eclipse_repo_root,	 'kernel',					paths['kernel'],				'org.eclipse.virgo.kernel',			bundle_version, release_from_branch,		'clean clean-integration test publish-ivy publish-maven'),
	Repository.new(virgo_eclipse_repo_root,	 'kernel-tools',			paths['kernel-tools'],			'org.eclipse.virgo.kernel-tools',	bundle_version, release_from_branch,		default_targets),
	Repository.new(gemini_eclipse_repo_root, 'gemini-web-container',	paths['gemini-web'],			'org.eclipse.gemini.web',			gemini_version, gemini_release_from_branch, 'clean clean-integration test doc package publish-ivy publish-maven'),
	Repository.new(virgo_eclipse_repo_root,	 'web',						paths['web'],					'org.eclipse.virgo.web',			bundle_version, release_from_branch,		default_targets),
	Repository.new(virgo_eclipse_repo_root,	 'snaps',					paths['snaps'],					'org.eclipse.virgo.snaps',			bundle_version, release_from_branch,		'clean clean-integration test package publish-ivy publish-maven publish-build publish-download'),
	Repository.new(virgo_eclipse_repo_root,	 'apps',					paths['apps'],					'org.eclipse.virgo.apps',			bundle_version, release_from_branch,		default_targets),
	Repository.new(virgo_eclipse_repo_root,	 'documentation',			paths['documentation'],			'org.eclipse.virgo.documentation',	bundle_version, release_from_branch,		'clean clean-integration doc-html package publish-ivy publish-download'),
    Repository.new(virgo_eclipse_repo_root, 'packaging',                      paths['packaging'],             'org.eclipse.virgo.packaging',		bundle_version,        
        release_from_branch,        'clean clean-integration test package-signed smoke-test publish-ivy publish-packages-build publish-packages-download publish-updatesite-download')
  ]

elsif args[:product_release] == 'kernel'

  ALL_REPOS = [
	Repository.new(virgo_eclipse_repo_root, 'util',						paths['util'],					'org.eclipse.virgo.util',			bundle_version, release_from_branch,		clean_all_targets),
	Repository.new(virgo_eclipse_repo_root, 'test',						paths['test'],					'org.eclipse.virgo.test',			bundle_version, release_from_branch,		default_targets),
	Repository.new(virgo_eclipse_repo_root, 'medic',					paths['medic'],					'org.eclipse.virgo.medic',			bundle_version, release_from_branch,		default_targets),
	Repository.new(virgo_eclipse_repo_root,	 'nano',					paths['nano'],					'org.eclipse.virgo.nano',			bundle_version, release_from_branch,		'clean clean-integration test package publish-ivy publish-maven publish-multiple-nano-packages-build publish-updatesite-build publish-multiple-nano-packages-download'),
	Repository.new(virgo_eclipse_repo_root, 'artifact-repository',		paths['artifact-repository'],	'org.eclipse.virgo.repository',		bundle_version, release_from_branch,		default_targets),
	Repository.new(virgo_eclipse_repo_root, 'kernel',					paths['kernel'],				'org.eclipse.virgo.kernel',			bundle_version, release_from_branch,		'clean clean-integration test package smoke-test publish-ivy publish-maven publish-package-build publish-updatesite-build publish-package-download'),
	Repository.new(virgo_eclipse_repo_root, 'kernel-tools',				paths['kernel-tools'],			'org.eclipse.virgo.kernel-tools',	bundle_version, release_from_branch,		default_targets)
  ]

elsif args[:product_release] == 'web-server'

  ALL_REPOS = [
	Repository.new(virgo_eclipse_repo_root, 'web',						paths['web'],					'org.eclipse.virgo.web',			bundle_version, release_from_branch,		clean_all_targets),
	Repository.new(virgo_eclipse_repo_root, 'snaps',					paths['snaps'],					'org.eclipse.virgo.snaps',			bundle_version, release_from_branch,		'clean clean-integration test package publish-ivy publish-maven publish-build publish-download'),
	Repository.new(virgo_eclipse_repo_root, 'apps',						paths['apps'],					'org.eclipse.virgo.apps',			bundle_version, release_from_branch,		default_targets),
	Repository.new(virgo_eclipse_repo_root, 'documentation',			paths['documentation'],			'org.eclipse.virgo.documentation',	bundle_version, release_from_branch,		'clean clean-integration doc-html package publish-ivy publish-download'),
    Repository.new(virgo_eclipse_repo_root, 'packaging',                      paths['packaging'],             'org.eclipse.virgo.packaging',		bundle_version,        
        release_from_branch,        'clean clean-integration test package-signed smoke-test publish-ivy publish-packages-build publish-packages-download publish-updatesite-download')
  ]

elsif args[:product_release] == 'virgo'

  ALL_REPOS = [
	Repository.new(virgo_eclipse_repo_root, 'util',						paths['util'],					'org.eclipse.virgo.util',			bundle_version, release_from_branch,		clean_all_targets),
	Repository.new(virgo_eclipse_repo_root, 'test',						paths['test'],					'org.eclipse.virgo.test',			bundle_version, release_from_branch,		default_targets),
	Repository.new(virgo_eclipse_repo_root, 'medic',					paths['medic'],					'org.eclipse.virgo.medic',			bundle_version, release_from_branch,		default_targets),
	Repository.new(virgo_eclipse_repo_root,	 'nano',					paths['nano'],					'org.eclipse.virgo.nano',			bundle_version, release_from_branch,		'clean clean-integration test publish-ivy publish-maven'),
	Repository.new(virgo_eclipse_repo_root, 'artifact-repository',		paths['artifact-repository'],	'org.eclipse.virgo.repository',		bundle_version, release_from_branch,		default_targets),
	Repository.new(virgo_eclipse_repo_root, 'kernel',					paths['kernel'],				'org.eclipse.virgo.kernel',			bundle_version, release_from_branch,		'clean clean-integration test publish-ivy publish-maven'),
	Repository.new(virgo_eclipse_repo_root, 'kernel-tools',				paths['kernel-tools'],			'org.eclipse.virgo.kernel-tools',	bundle_version, release_from_branch,		default_targets),
	Repository.new(virgo_eclipse_repo_root, 'web',						paths['web'],					'org.eclipse.virgo.web',			bundle_version, release_from_branch,		default_targets),
	Repository.new(virgo_eclipse_repo_root, 'snaps',					paths['snaps'],					'org.eclipse.virgo.snaps',			bundle_version, release_from_branch,		'clean clean-integration test package publish-ivy publish-maven publish-build publish-download'),
	Repository.new(virgo_eclipse_repo_root, 'apps',						paths['apps'],					'org.eclipse.virgo.apps',			bundle_version, release_from_branch,		default_targets),
	Repository.new(virgo_eclipse_repo_root, 'documentation',			paths['documentation'],			'org.eclipse.virgo.documentation',	bundle_version, release_from_branch,		'clean clean-integration doc-html package publish-ivy publish-download'),
    Repository.new(virgo_eclipse_repo_root, 'packaging',                      paths['packaging'],             'org.eclipse.virgo.packaging',		bundle_version,        
        release_from_branch,        'clean clean-integration test package-signed smoke-test publish-ivy publish-packages-build publish-packages-download publish-updatesite-download')
  ]

else

  ALL_REPOS = [
	Repository.new(virgo_eclipse_repo_root, 'util',					paths['util'],					'org.eclipse.virgo.util',				bundle_version, release_from_branch,		clean_all_targets),
	Repository.new(virgo_eclipse_repo_root, 'test',					paths['test'],					'org.eclipse.virgo.test',				bundle_version, release_from_branch,		default_targets),
	Repository.new(virgo_eclipse_repo_root, 'medic',				paths['medic'],					'org.eclipse.virgo.medic',				bundle_version, release_from_branch,		default_targets),
	Repository.new(virgo_eclipse_repo_root, 'artifact-repository',	paths['artifact-repository'],	'org.eclipse.virgo.repository',			bundle_version, release_from_branch,		default_targets),
	Repository.new(virgo_eclipse_repo_root,	 'nano',				paths['nano'],					'org.eclipse.virgo.nano',				bundle_version, release_from_branch,		 'clean clean-integration test publish-ivy publish-maven'),
	Repository.new(virgo_eclipse_repo_root, 'artifact-repository',	paths['artifact-repository'],	'org.eclipse.virgo.repository',			bundle_version, release_from_branch,		default_targets),
	Repository.new(virgo_eclipse_repo_root, 'kernel',				paths['kernel'],				'org.eclipse.virgo.kernel',				bundle_version, release_from_branch,		'clean clean-integration test publish-ivy publish-maven'),
	Repository.new(virgo_eclipse_repo_root, 'kernel-tools',			paths['kernel-tools'],			'org.eclipse.virgo.kernel-tools',		bundle_version, release_from_branch,		default_targets),
	Repository.new(virgo_eclipse_repo_root, 'web',					paths['web'],					'org.eclipse.virgo.web',				bundle_version, release_from_branch,		default_targets),
	Repository.new(virgo_eclipse_repo_root, 'snaps',				paths['snaps'],					'org.eclipse.virgo.snaps',				bundle_version, release_from_branch,		'clean clean-integration test package publish-ivy publish-maven publish-build publish-download'),
	Repository.new(virgo_eclipse_repo_root, 'apps',					paths['apps'],					'org.eclipse.virgo.apps',				bundle_version, release_from_branch,		default_targets),
	Repository.new(virgo_eclipse_repo_root, 'documentation',		paths['documentation'],			'org.eclipse.virgo.documentation',		bundle_version, release_from_branch,		'clean clean-integration doc-html package publish-ivy publish-download'),
    Repository.new(virgo_eclipse_repo_root, 'packaging',                      paths['packaging'],             'org.eclipse.virgo.packaging',		bundle_version,        
        release_from_branch,        'clean clean-integration test package-signed smoke-test publish-ivy publish-packages-build publish-packages-download publish-updatesite-download')
    ]

end

log_file=File.expand_path('./release.log')
start_time = Time.new
accumulate_versions = Hash.new
console = Console.new

begin

  ALL_REPOS.each do |repo|
	puts 'Releasing ' + repo.name
	puts '    checkout with "' + repo.clone_command + '"' if DRY_RUN
	console.set_title(SCRIPT_NAME, "#{repo.name} Checkout")
	repo.checkout(true)
	if DRY_RUN
	  puts "  Create Release branch " + args[:version] + ", " + args[:build_stamp] + ", " + args[:release_type]
	  puts "	using versions: "
	  accumulate_versions.sort.each {|keyval| puts "	  " + keyval[0] + " = " + keyval[1]}
	  if !args[:build_version].nil?
		puts '	updating Virgo Build to \'' + args[:build_version] + '\''
	  end
	  puts "  Building " + repo.name
	  puts "  Create tag " + repo.bundle_version
	  puts "  Update Master branch " + args[:new_version]
	else
	  console.set_title(SCRIPT_NAME, "#{repo.name} Create release branch")
	  if repo.name == 'gemini-web-container'
		repo.create_release_branch(args[:gemini_version], args[:gemini_build_stamp], args[:gemini_release_type], accumulate_versions)
	  else
		repo.create_release_branch(args[:version], args[:build_stamp], args[:release_type], accumulate_versions)
	  end
	  if !args[:build_version].nil?
		console.set_title(SCRIPT_NAME, "#{repo.name} Update Virgo build")
		repo.update_virgo_build(args[:build_version])
	  end
	  console.set_title(SCRIPT_NAME, "#{repo.name} Build")
	  repo.build(args[:remote_user], log_file)
	  console.set_title(SCRIPT_NAME, "#{repo.name} Create tag")
	  repo.create_tag
	  console.set_title(SCRIPT_NAME, "#{repo.name} Update master branch")
	  if repo.name == 'gemini-web-container'
		repo.update_master_branch(args[:gemini_new_version], accumulate_versions)
	  else
		repo.update_master_branch(args[:new_version], accumulate_versions)
	  end
	end
	accumulate_versions = (repo.versions).merge(accumulate_versions)
  end

  console.set_title(SCRIPT_NAME, "Push?")

  if !DRY_RUN
	puts 'Execution Time: ' + Time.at(Time.new - start_time).utc.strftime('%R:%S')
	puts ''

	print 'Do you want to push? (y/n) '
	commit_ok = STDIN.gets.chomp
	if commit_ok =~ /y.*/
	  ALL_REPOS.each do |repo|
		console.set_title(SCRIPT_NAME, "#{repo.name} Push")
		if repo.name == 'gemini-web-container'
		  repo.push(args[:gemini_new_version])
		else
		  repo.push(args[:new_version])
		end
	  end
	end
  end

  console.clear_title

rescue
  console.set_title(SCRIPT_NAME, "Error")
end

