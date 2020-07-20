.PHONY: default
default:
	@echo "Use 'make test' to exercise the example"

.PHONY: test
test:
	mvn package
	scripts/test-example.sh
