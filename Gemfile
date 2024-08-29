source "https://rubygems.org"

ENV['LC_ALL'] = 'en_US.UTF-8'
ENV['LANG'] = 'en_US.UTF-8'

gem "fastlane"
gem 'mutex_m'
gem 'abbrev'
gem 'bigdecimal'

plugins_path = File.join(File.dirname(__FILE__), 'fastlane', 'Pluginfile')
eval_gemfile(plugins_path) if File.exist?(plugins_path)
