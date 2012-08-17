#!/usr/bin/env ruby -wKU
$LOAD_PATH << File.expand_path(File.dirname(__FILE__))

require 'rubygems'
require 'choice'
require 'lib/version'
require 'fileutils'

Choice.options do

  header('')
  header('Required arguments:')

  option :variable, :required => true do
    short('-v')
    long('--variable=VARIABLE')
    desc('The variable name to update')
  end

  option :new_version, :required => true do
    short('-n')
    long('--new-version=NEW-VERSION')
    validate(/\d(.\d(.\d(.([\w_-])+)?)?)?/)
    desc('The version to update to')
  end

end

args = Choice.choices
Version.update(args[:variable], args[:new_version], FileUtils.pwd, true)
