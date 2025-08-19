JAVAC = javac
JAVA  = java
RM    = rm -f

SOURCES = Calculator.java CalculatorImplementation.java CalculatorServer.java CalculatorClient.java
CLASSES = $(SOURCES:.java=.class)

all: $(CLASSES)

%.class: %.java
	$(JAVAC) $<

run-server: CalculatorServer.class
	$(JAVA) CalculatorServer

run-client: CalculatorClient.class
	$(JAVA) CalculatorClient $(HOST)

clean:
	$(RM) *.class
