#!/bin/bash
curl --request POST \
  --url http://127.0.0.1:8080/fhir/Organization \
  --header 'content-type: application/json' \
  --data '{
	"resourceType": "Organization",
	"type": "prov",
	"name": "Root Orga"
}'

curl --request POST \
  --url http://127.0.0.1:8080/fhir/Organization \
  --header 'content-type: application/json' \
  --data '{
	"resourceType": "Organization",
	"type": "prov",
	"name": "CENTRAL Orga",
	"partOf": {
		"type": "Organization",
		"reference": "Organization/1"
	}
}'

curl --request POST \
  --url http://127.0.0.1:8080/fhir/Organization \
  --header 'content-type: application/json' \
  --data '{
	"resourceType": "Organization",
	"type": "prov",
	"name": "MYRIS Orga",
	"partOf": {
		"type": "Organization",
		"reference": "Organization/1"
	}
}'

curl --request POST \
  --url http://127.0.0.1:8080/fhir/Organization \
  --header 'content-type: application/json' \
  --data '{
	"resourceType": "Organization",
	"type": "prov",
	"name": "AOIS Orga",
	"partOf": {
		"type": "Organization",
		"reference": "Organization/1"
	}
}'

curl --request POST \
  --url http://127.0.0.1:8080/fhir/Organization \
  --header 'content-type: application/json' \
  --data '{
	"resourceType": "Organization",
	"type": "prov",
	"name": "Doctolib Orga",
	"partOf": {
		"type": "Organization",
		"reference": "Organization/1"
	}
}'

curl --request PUT \
  --url http://127.0.0.1:8080/fhir/Organization/2 \
  --header 'content-type: application/json' \
  --data '{
	"resourceType": "Organization",
	"id": 2,
	"type": "prov",
	"name": "Central Orga",
	"partOf": {
		"type": "Organization",
		"reference": "Organization/1"
	},
	"identifier": [
		{
			"type": {
				"coding": [ {
        "system": "http://hl7.org/fhir/v2/0203",
        "code": "XX"
      } ]
			},
			"value": "CENTRAL",
			"assigner":{
				"type": "Organization",
				"reference": "Organization/2"
			}
		}
	]
}'

curl --request PUT \
  --url http://127.0.0.1:8080/fhir/Organization/3 \
  --header 'content-type: application/json' \
  --data '{
	"resourceType": "Organization",
	"id": 3,
	"type": "prov",
	"name": "MYRIS Orga",
	"partOf": {
		"type": "Organization",
		"reference": "Organization/1"
	},
	"identifier": [
		{
			"type": {
				"coding": [ {
        "system": "http://hl7.org/fhir/v2/0203",
        "code": "XX"
      } ]
			},
			"value": "MYRIS",
			"assigner":{
				"type": "Organization",
				"reference": "Organization/3"
			}
		}
	]
}'

curl --request PUT \
  --url http://127.0.0.1:8080/fhir/Organization/4 \
  --header 'content-type: application/json' \
  --data '{
  "resourceType": "Organization",
  "id": "4",
	"type": "prov",
  "name": "AOIS Orga",
  "partOf": {
    "reference": "Organization/1",
    "type": "Organization"
  },
	"identifier": [
    {
      "type": {
        "coding": [
          {
            "system": "http://hl7.org/fhir/v2/0203",
            "code": "XX"
          }
        ]
      },
      "value": "AOIS",
      "assigner": {
        "reference": "Organization/4",
        "type": "Organization"
      }
    }
  ],
	"alias": [
		"AOIS"
	]
}'

curl --request PUT \
  --url http://127.0.0.1:8080/fhir/Organization/5 \
  --header 'content-type: application/json' \
  --data '{
  "resourceType": "Organization",
  "id": "5",
	"type": "prov",
  "name": "Doctolib Orga",
  "partOf": {
    "reference": "Organization/1",
    "type": "Organization"
  },
	"identifier": [
    {
      "type": {
        "coding": [
          {
            "system": "http://hl7.org/fhir/v2/0203",
            "code": "XX"
          }
        ]
      },
      "value": "Doctolib",
      "assigner": {
        "reference": "Organization/5",
        "type": "Organization"
      }
    }
  ],
	"alias": [
		"Doctolib"
	]
}'

