
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public class DBEngine {
    enum Entity {
        STUDENTS(0), GROUPS(1), SUBJECTS(2), MARKS(3);
        final int position;

        Entity(int position) {
            this.position = position;
        }
    }

    enum Parameter {
        SELECT(0), FROM(1), WHERE(2), GROUP_BY(3);
        final int position;

        Parameter(int position) {
            this.position = position;
        }
    }
    public boolean isFound(String command, String regex){ //проверяем есть ли внутри
        return Pattern.compile(regex).matcher(command).matches();
    }

    private final List<Parameter> parameters = new ArrayList<>();
    private String whereParameters, groupByParameters;
    private String[] selectParameterNames;
    private final Multimap<String, String> namesAndValuesFromWhereParameter = ArrayListMultimap.create();

    private final Map<String, Set<String>> selectEntitiesAndAttributes = new HashMap<>();
    private final Map<String, Set<String>> whereEntitiesAndAttributes = new HashMap<>();
    private final Map<String, String> lineNamesAndValues = new HashMap<>();

    private final WhereCalc whereCalculator = new WhereCalc();

    private final Map<String, String> filesNamesAndEntities = new HashMap<>();

    private final String COMMAND_FILE = "src/main/resources/saving/command.txt";
    private final String CACHE_FILE = "src/main/resources/saving/cache.txt";

    private boolean newFiles;

    private final StringBuilder selectedLines = new StringBuilder();

    private final StringBuilder commandResult = new StringBuilder();

    private int commandNumber;

    private boolean filesEquals(List<File> newFiles, List<File> previousFiles) {
        if (newFiles.size() != previousFiles.size()) {
            return false;
        }

        for (int i = 0; i < newFiles.size(); i++) {
            if (newFiles.get(i).lastModified() != previousFiles.get(i).lastModified())
                return false;
        }
        return true;
    }

    DBEngine(List<File> files) {
        filesNamesAndEntities.put("STUDENTS", "src/main/resources/saving/students.csv");
        filesNamesAndEntities.put("GROUPS", "src/main/resources/saving/groups.csv");
        filesNamesAndEntities.put("SUBJECTS", "src/main/resources/saving/subjects.csv");
        filesNamesAndEntities.put("MARKS", "src/main/resources/saving/marks.csv");

        checkFiles(files);
    }

    public void checkFiles(List<File> inputFiles) {
        List<File> savedFiles = new ArrayList<>();
        for (Entity entity : Entity.values())
            savedFiles.add(new File(filesNamesAndEntities.get(entity.toString())));

        newFiles = !filesEquals(inputFiles, savedFiles);
        if (newFiles) {
            System.out.println("Сохранение файлов");
            try {
                copyFiles(inputFiles);
                try (FileWriter ignored = new FileWriter(COMMAND_FILE)) {}
                try (FileWriter ignored = new FileWriter(CACHE_FILE)) {}
            } catch (IOException error) {
                error.printStackTrace();
            }
        }
    }

    public void run(String command) {
        boolean needSaveCommand = false;
        try {
            command = command.toUpperCase();
            needSaveCommand = !commandsEquals(command);

            if (newFiles || needSaveCommand) { // если у нас обновились файлики или новая команда
                exec(command);
            }
            else
                getCacheResult();
            System.out.println(commandResult);
        } catch (RuntimeException | IOException error) {
            error.printStackTrace();
            return;
        }
        try {
            if (needSaveCommand) {
                System.out.println("Сохранение команды\n");
                saveCommand(command);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getCacheResult() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(CACHE_FILE))) {
            String result;
            int lineNumber = 0;
            while ((result = reader.readLine()) != null) {
                lineNumber++;
                if (lineNumber == commandNumber) {
                    commandResult.setLength(0);
                    commandResult.append(result.replace('\t', '\n')).append('\n');
                    return;
                }
            }
            throw new RuntimeException("Ошибка поиска в кэше!");
        }
    }

    private boolean commandsEquals(String command) throws IOException {
        commandNumber = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(COMMAND_FILE))) {
            String previousCommand;
            while ((previousCommand = reader.readLine()) != null) {
                commandNumber++;
                if (previousCommand.equals(command))
                    return true;
            }
            return false;
        }
    }

    private void copyFiles(List<File> files) throws IOException {
        for (Entity entity : Entity.values()) {
            Optional<File> fileOptional = Optional.ofNullable(files.get(entity.position)); // оздание значения Optional для объекта, который может быть нулевым (null).
            Files.copy(Path.of(fileOptional.orElseThrow(() -> new RuntimeException("Null input file")).getPath()),
                    new FileOutputStream(filesNamesAndEntities.get(entity.toString())));
            fileOptional.get().setLastModified(new File(filesNamesAndEntities.get(entity.toString())).lastModified()); // задаем новым файликам чпараметр последнего изменения т.е мы их перекопировали
        }
    }

    private void saveCommand(String command) throws IOException {
        try (FileWriter writer = new FileWriter(COMMAND_FILE, true)) {
            writer.write(command + '\n');
        }
    }

    private void writeCache() throws IOException {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(CACHE_FILE, true))) {
            commandResult.deleteCharAt(commandResult.length() - 1);
            bufferedWriter.write(commandResult.toString().replace('\n', '\t'));
            bufferedWriter.newLine();
        }
    }

    private void exec(String command) throws IOException {
        parameters.clear();
        lineNamesAndValues.clear();
        List<String> parameters = getParameters(getRegex(command), command); // получаем внутренние параметры для селект фром и тд
        String selectParameters = parameters.get(Parameter.SELECT.position);
        String fromParameters = parameters.get(Parameter.FROM.position);
        whereParameters = parameters.get(Parameter.WHERE.position);
        groupByParameters = parameters.get(Parameter.GROUP_BY.position);
        whereCalculator.setExpression(whereParameters);//передать параметры и переделать их в префиксную форму(без пробелов)
        saveSelectParameters(selectParameters); //записываем в мапу таблица - поля
        saveWhereParameters(whereParameters);
        LinkedList<String> entityNamesFromFromParameter = returnFromParameters(fromParameters);

        if ((groupByParameters != null) && !groupByAndSelectEquals())
            throw new RuntimeException("Параметры SELECT и GROUP BY некорректны!");

        writeAttributeNames();
        selectedLines.setLength(0);
        parsing(entityNamesFromFromParameter);
        writeCache();

    }

    private String getRegex(String command) {
        parameters.add(Parameter.SELECT);
        parameters.add(Parameter.FROM);
        String commandRegex = "^SELECT\\s(.+)\\sFROM\\s(.+)";

        String whereRegex = commandRegex + "\\sWHERE\\s(.+)";
        if (isFound(command, whereRegex)) {
            commandRegex = whereRegex;
            parameters.add(Parameter.WHERE);
        }

        String groupByRegex = commandRegex + "\\sGROUP BY\\s(.+)$";
        if (isFound(command, groupByRegex)) {
            commandRegex = groupByRegex;
            parameters.add(Parameter.GROUP_BY);
        }
        return commandRegex;
    }

    private List<String> getParameters(String regexParameters, String command) {
        Pattern pattern = Pattern.compile(regexParameters);
        Matcher matcher = pattern.matcher(command);

        List<String> parameters = Arrays.asList(null, null, null, null);
        if (matcher.find()) {
            int paramPos = 1;
            for (Parameter parameter : this.parameters)
                parameters.set(parameter.position, matcher.group(paramPos++));
        }
        return parameters;
    }

    private boolean groupByAndSelectEquals()
    {
        Set<String> fullAttributesNameInGroupByParameter = Arrays.stream(groupByParameters.split("\s*,\s*"))
                .collect(Collectors.toSet());

        if (fullAttributesNameInGroupByParameter.size() != selectParameterNames.length)
            return false;
        for (String fullAttributeName : selectParameterNames) {
            if (!fullAttributesNameInGroupByParameter.contains(fullAttributeName))
                return false;
        }
        return true;
    }

    private void saveSelectParameters(String selectParameter) {
        selectEntitiesAndAttributes.clear();
        if (selectParameter == null)
            throw new RuntimeException("Нет выводимых полей!");

        selectParameterNames = selectParameter.split("\\s*,\\s+"); // разбиваем по запятой с неопр кол-вом пробелов до и после
        for (String entityAndAttributeName : selectParameterNames) {
            String[] oneEntityAndOneAttributeName = entityAndAttributeName.split("\\.");
            try {
                insertInMap(selectEntitiesAndAttributes, oneEntityAndOneAttributeName); // формируем map таблица -> {список полей}
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new RuntimeException("Некорректная команда: " + selectParameter);
            }
        }
    }

    private void saveWhereParameters(String whereParameter) {
        whereEntitiesAndAttributes.clear();
        namesAndValuesFromWhereParameter.clear();
        if (whereParameter == null)
            return;

        for (String logicalExpression : whereParameter.split("\\s+(AND|OR)\\s+"))
        {
            Matcher logicalExpressionMatcher = Pattern.compile("\\s*((.+)\\.(.+\\S))\\s*=\\s*(\\S.*)") // разбиваем строку на отдельные элементы
                    .matcher(logicalExpression);
            if (!logicalExpressionMatcher.find())
                throw new RuntimeException("Некорректный WHERE параметр: " + whereParameter);

            try {
                int fullAttributeNamePos = 1, entityPos = 2, attributePos = 3, valueAfterEqualsPos = 4;
                Optional<String> fullAttributeNameOptional = Optional.ofNullable(logicalExpressionMatcher.group(fullAttributeNamePos));
                Optional<String> entityOptional = Optional.ofNullable(logicalExpressionMatcher.group(entityPos));
                Optional<String> attributeOptional = Optional.ofNullable(logicalExpressionMatcher.group(attributePos));
                Optional<String> valueAfterEqualsOptional = Optional.ofNullable(logicalExpressionMatcher.group(valueAfterEqualsPos));

                String fullAttributeName = fullAttributeNameOptional.orElseThrow(()->new RuntimeException("Не удаётся получить имя атрибута: " + logicalExpression));
                String entity = entityOptional.orElseThrow(()->new RuntimeException("Не удаётся получить сущность: " + logicalExpression));
                String attribute = attributeOptional.orElseThrow(()->new RuntimeException("Не удаётся получить атрибут: " + logicalExpression));
                String valueAfterEquals = valueAfterEqualsOptional.orElseThrow(()->new RuntimeException("Не удаётся получить значение: " + logicalExpression));

                insertInMap(whereEntitiesAndAttributes, new String[]{entity, attribute}); // map таблица->параметр для where

                namesAndValuesFromWhereParameter.put(fullAttributeName, valueAfterEquals); //пишем в мап
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new RuntimeException("Некорректная команда: " + whereParameter);
            }
        }
    }
    private void insertInMap(Map<String, Set<String>> entitiesNamesAndAttributeNames,
                             String[] entityAndAttributeName) // map таблица - поля, string[] - таблица - поле
    {
        final int positionOfEntity = 0, positionOfParameter = 1;
        String entity = entityAndAttributeName[positionOfEntity].toUpperCase();
        String attribute = entityAndAttributeName[positionOfParameter].toUpperCase();

        Set<String> attributesOfEntities = entitiesNamesAndAttributeNames.get(entity);
        if (attributesOfEntities == null) // если такой таблицы у нас не было, то создаем новый сет
            attributesOfEntities = new HashSet<>();
        attributesOfEntities.add(attribute);
        entitiesNamesAndAttributeNames.put(entity, attributesOfEntities);
    }
    private LinkedList<String> returnFromParameters(String from) {
        if (from == null)
            throw new RuntimeException("Отсутствует FROM-таблица для получения данных!");

        String[] separatedFrom = from.split("\\s*,\\s+");
        return Arrays.stream(separatedFrom).collect(Collectors.toCollection(LinkedList::new));
    }

    private void writeAttributeNames() {
        commandResult.setLength(0);
        for (String fullAttributeName : selectParameterNames)
            commandResult.append(fullAttributeName).append(' ');
        commandResult.setCharAt(commandResult.length() - 1, '\n');
    }

    private void parsing(LinkedList<String> entityNamesFromFromParameter) throws IOException
    {
        if ((entityNamesFromFromParameter == null) || (entityNamesFromFromParameter.size() == 0))
            throw new RuntimeException("Отсутствует SELECT команда!");

        String entityName = entityNamesFromFromParameter.getLast();
        Optional<String> fileNameOptional = Optional.ofNullable(filesNamesAndEntities.get(entityName));
        String fileName = fileNameOptional.orElseThrow(() -> new RuntimeException("Некорректная сущность: " + entityName));

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName)))
        {
            String line;
            if ((line = bufferedReader.readLine()) == null)
                throw new RuntimeException("Нет атрибутов в файле: " + fileName);
            line = line.toUpperCase();

            Map<Integer, String> namesAndPos = getNamesAndPos(entityName, line, selectEntitiesAndAttributes);
            //проходимся по первой строке в файле и делаем мап номера атрибута и его полное имя из селекта
            namesAndPos.putAll(getNamesAndPos(entityName, line, whereEntitiesAndAttributes));
            // добавляем в мап еше параметры из вере
            while ((line = bufferedReader.readLine()) != null)
            {
                line = line.toUpperCase();
                saveDomainsForSelectAndWhere(line, namesAndPos);
                // запомнили для текущей строки полное название атрибутов и их значения
                if (entityNamesFromFromParameter.size() == 1) {
                    boolean whereParameterIsPassed = true;
                    if (whereParameters != null) {
                        Map<String, Integer> mapForCalculator = returnMapForCalculator();
                        whereParameterIsPassed = (whereCalculator.countExpression(mapForCalculator) >= 1);
                    }

                    String saveLine = createSaveLine();
                    boolean groupByParameterIsPassed = true;
                    if (groupByParameters != null)
                        groupByParameterIsPassed = !containsLine(saveLine);

                    if (whereParameterIsPassed && groupByParameterIsPassed) {
                        commandResult.append(saveLine).append('\n');
                        if (groupByParameters != null)
                            selectedLines.append(saveLine).append('\n');
                    }
                } else {
                    String nextFileName = entityNamesFromFromParameter.removeLast(); // удаляем файлик который прочитали
                    parsing(entityNamesFromFromParameter);
                    entityNamesFromFromParameter.add(nextFileName);
                }
            }
        }
    }
    private Map<Integer, String> getNamesAndPos(String entityName, String firstLine, Map<String, Set<String>> entitiesNamesAndAttributes)
    {
        Map<Integer, String> fullDomainNameAndItsPos = new HashMap<>();
        if (entitiesNamesAndAttributes.containsKey(entityName) && (firstLine != null))
        {
            Set<String> neededAttributes = entitiesNamesAndAttributes.get(entityName);

            int position = 0;
            for (String attributeName : firstLine.split("\s*,\s*")) {
                attributeName = attributeName.toUpperCase();
                if (neededAttributes.contains(attributeName))
                    fullDomainNameAndItsPos.put(position, entityName + "." + attributeName);
                position++;
            }
        }
        return fullDomainNameAndItsPos;
    }
    private void saveDomainsForSelectAndWhere(String domainLine, Map<Integer, String> namesAndPos)
    {
        String[] domains = domainLine.split("\s*,\s*");
        for (Integer positionOfNeededDomain : namesAndPos.keySet()) {
            lineNamesAndValues.put(namesAndPos.get(positionOfNeededDomain),
                    domains[positionOfNeededDomain]);
        }
    }

    private Map<String, Integer> returnMapForCalculator() {
        //тут мы формируем мап для каждой колоночки из нужных и смотрим в прочитанных значениях что подошло а что нет
        Map<String, Integer> logicalExpressionsAndItsValues = new HashMap<>();
        for (String fullAttributeName : namesAndValuesFromWhereParameter.keySet()) { //значение -> значения типа (Marks.id -> 5)
            if (!lineNamesAndValues.containsKey(fullAttributeName))
                throw new RuntimeException("Неизвестный WHERE-параметр: " + fullAttributeName);

            String attributeValueFromLine = lineNamesAndValues.get(fullAttributeName);// доставем коекретное значение параметра
            Collection<String> attributeValuesFromWhereParameter = namesAndValuesFromWhereParameter.get(fullAttributeName);
            // достаем значение параметров после равно для данного атрибута
            for (String attributeValueFromWhereParameter : attributeValuesFromWhereParameter) {
                int logicalExpressionValue = attributeValueFromLine.equals(attributeValueFromWhereParameter) ? 1 : 0;
                //если значение в строке совпало со значением в запросе то ставим 1 иначе 0
                boolean attributeValueIsDigit = attributeValueFromWhereParameter.chars().allMatch(Character::isDigit);
                StringBuilder stringBuilderToBuildLogicalExpression = new StringBuilder();
                stringBuilderToBuildLogicalExpression.append(fullAttributeName).append('=');
                //if (!attributeValueIsDigit)
                //    stringBuilderToBuildLogicalExpression.append('\'');
                stringBuilderToBuildLogicalExpression.append(attributeValueFromWhereParameter);
                //if (!attributeValueIsDigit)
                //    stringBuilderToBuildLogicalExpression.append('\'');
                logicalExpressionsAndItsValues.put(stringBuilderToBuildLogicalExpression.toString(), logicalExpressionValue);
            }
        }
        return logicalExpressionsAndItsValues;
    }
    private String createSaveLine() { //создаем линию для сохранение в кэш
        StringBuilder stringBuilderToCreateLine = new StringBuilder();
        for (String fullAttributesName : selectParameterNames) {
            if (!lineNamesAndValues.containsKey(fullAttributesName))
                throw new RuntimeException("Некорректный SELECT-параметр: " + fullAttributesName);
            stringBuilderToCreateLine.append(lineNamesAndValues.get(fullAttributesName));
            stringBuilderToCreateLine.append(' ');
        }
        if (stringBuilderToCreateLine.length() != 0)
            stringBuilderToCreateLine.deleteCharAt(stringBuilderToCreateLine.length() - 1);
        return stringBuilderToCreateLine.toString();
    }

    private boolean containsLine(String lineToFind) {
        if (selectedLines.length() == 0)
            return false;
        try (Stream<String> streamLine = Arrays.stream(selectedLines.toString().split("\n"))) {
            if (streamLine.anyMatch((line) -> line.equals(lineToFind)))
                return true;
        }
        return false;
    }
}