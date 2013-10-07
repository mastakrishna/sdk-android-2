#!/usr/bin/env ruby

require "rubygems"
require "json"
require "typhoeus"
require 'zip/zip'

APPTHWACK_API_KEY=ENV['APPTHWACK_API_KEY']
if APPTHWACK_API_KEY == nil
	puts "Please set APPTHWACK_API_KEY.\n"
	exit 100
end

def get_project_id(name)
	request = Typhoeus::Request.new(
		"https://appthwack.com/api/project/",
		method: :get,
		userpwd: "#{APPTHWACK_API_KEY}:"
	)
	request.run
	response = request.response
	string = response.body
	json = JSON.parse(string)
	projectId=nil
	json.each do |project|
		if project["name"] == name
			projectId=project['id']
			break
		end
	end
	return projectId
end

def get_device_pool(projectId, name)
	request = Typhoeus::Request.new(
		"https://appthwack.com/api/devicepool/#{projectId}",
		method: :get,
		userpwd: "#{APPTHWACK_API_KEY}:"
	)
	request.run
	response = request.response
	string = response.body
	json = JSON.parse(string)
	poolId=nil
	json.each do |pool|
		if pool["name"] == name
			poolId=pool['id']
			break
		end
	end
	return poolId
end

def upload_diagnostic_app
	diagFile = nil
	Dir.glob('diagnostic/target/*.apk') do |file|
		diagFile=file
		break
	end
	diagFileName=File.basename(diagFile)
	request = Typhoeus::Request.new(
		"https://appthwack.com/api/file",
		method: :post,
		userpwd: "#{APPTHWACK_API_KEY}:",
		params: {
			name: "#{diagFileName}"
		},
		body: {
			file: File.open("#{diagFile}","r")
		}
	)
	request.run
	response = request.response
	string = response.body
	json = JSON.parse(string)
	if json["message"] != nil
		puts "Diagnostic Tests failed: #{json['message']}\n"
		return nil
	end
	return json["file_id"]
end

def upload_integration_app
	integFile = nil
	Dir.glob('integration/target/*.apk') do |file|
		integFile=file
		break
	end
	integFileName=File.basename(integFile)
	request = Typhoeus::Request.new(
		"https://appthwack.com/api/file",
		method: :post,
		userpwd: "#{APPTHWACK_API_KEY}:",
		params: {
			name: "#{integFileName}"
		},
		body: {
			file: File.open("#{integFile}","r")
		}
	)
	request.run
	response = request.response
	string = response.body
	json = JSON.parse(string)
	if json["message"] != nil
		puts "Integration Tests failed: #{json['message']}\n"
		return nil
	end
	return json["file_id"]
end

def start_test(name, projectId, appId, testId, poolId)
	request = Typhoeus::Request.new(
		"https://appthwack.com/api/run",
		method: :post,
		userpwd: "#{APPTHWACK_API_KEY}:",
		params: {
			project: "#{projectId}",
			name: "#{name}",
			app: "#{appId}",
			junit: "#{testId}",
			pool: "#{poolId}"
		}
	)
	request.run
	response = request.response
	string = response.body
	json = JSON.parse(string)
	if json["message"] != nil
		puts "RUN failed: #{json['message']}\n"
		return nil
	end
	return json["run_id"]
end

def test_running(projectId, runId)
	request = Typhoeus::Request.new(
		"https://appthwack.com/api/run/#{projectId}/#{runId}/status",
		method: :get,
		userpwd: "#{APPTHWACK_API_KEY}:",
	)
	request.run
	response = request.response
	string = response.body
	json = JSON.parse(string)
	if json["message"] != nil
		puts "STATUS failed: #{json['message']}\n"
		return false
	end
	return json["status"] != "completed"
end

def download_url(url, filename)
	request = Typhoeus::Request.new(
		url,
		method: :get
	)
	request.on_complete do |response|
		File.open(filename, 'w') { |f| f.write(response.body) }
		return filename
	end
	request.run
end

def download_results(projectId, runId)
	name = "#{projectId}_#{runId}.zip"
	request = Typhoeus::Request.new(
		"https://appthwack.com/api/run/#{projectId}/#{runId}",
		method: :get,
		userpwd: "#{APPTHWACK_API_KEY}:",
		params: {
			format: "archive"
		}
	)
	request.on_complete do |response|
		if response.code == 303
			return download_url response.headers_hash['Location'], name
		else
			print "Err #{response.code}: #{response.body}\n"
		end
	end
	request.run
end

projectId = get_project_id "Playhaven Android Diagnostic App"
print "Project ID: #{projectId}\n"
if projectId == nil
	print "Unable to locate project\n"
	exit 1
end

poolId = get_device_pool projectId, "Top 10 devices"
print "Pool ID: #{poolId}\n"
if poolId == nil
	print "Unable to locate pool\n"
	exit 2
end

diagAppId = upload_diagnostic_app
print "Diagnostic APK ID: #{diagAppId}\n"
if diagAppId == nil
	print "Unable to upload diagnostic app\n"
	exit 3
end

integAppId = upload_integration_app
print "Integration APK ID: #{integAppId}\n"
if integAppId == nil
	print "Unable to upload integration app\n"
	exit 4
end

runId = start_test "Android SDKv2", projectId, diagAppId, integAppId, poolId
print "Run ID: #{runId}\n"
if runId == nil
	print "Unable to start tests\n"
	exit 5
end

sleep 5
while test_running projectId, runId
	print "."
	sleep 30
end
print "\n"

sleep 60 # quiet to cut down on Err 500: {"message": "The results archive cannot be downloaded until testing is completed"}
zip = download_results projectId, runId
if zip == nil
	print "Unable to download results\n"
	exit 6
end
print "ZIP: #{zip}\n\n"
Zip::ZipFile.foreach(File.expand_path("#{zip}")) do |entry|
	if /\.instrtxt$/.match(entry.name) then
	    istream = entry.get_input_stream
	    data = istream.read
	    segment=/Time.*\n\n(.*)/.match(data)
	    if /^FAIL/.match(segment[1]) then
			print "#{segment[1]} #{entry.name}\n"
		end
	end
end


