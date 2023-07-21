package com.pavikumbhar.batchprocessing.batch.job;

import java.util.Arrays;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.transform.PassThroughLineAggregator;
import org.springframework.batch.item.support.ClassifierCompositeItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.classify.Classifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

/**
 *
 * @author pavikumbhar
 *
 */
@Configuration
@EnableBatchProcessing
public class ClassifiierFileBatchConguration {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Bean
    public ItemReader<Person> itemReader() {
        Person foo1 = new Person();
        foo1.setId(1);
        foo1.setName("Pravin");//
        Person foo2 = new Person();
        foo2.setId(2);
        foo2.setName("Mangesh");//
        Person bar1 = new Person();
        bar1.setId(3);
        bar1.setName("Pravin");//
        Person bar2 = new Person();
        bar2.setId(4);
        bar2.setName("Mangesh");
        return new ListItemReader<>(Arrays.asList(foo1, foo2, bar1, bar2));//
    }

    @Bean
    public ClassifierCompositeItemWriter<Person> classifierCompositeItemWriter(ItemWriter<Person> fooItemWriter, ItemWriter<Person> barItemWriter) {
        ClassifierCompositeItemWriter<Person> classifierCompositeItemWriter = new ClassifierCompositeItemWriter<>();
        classifierCompositeItemWriter.setClassifier((Classifier<Person, ItemWriter<? super Person>>) person -> {
            if (person.getName().startsWith("P")) {
                return fooItemWriter;
            } else {
                return barItemWriter;
            }
        });
        return classifierCompositeItemWriter;
    }

    @Bean
    public FlatFileItemWriter<Person> fooItemWriter() {
        return new FlatFileItemWriterBuilder<Person>().name("fooItemWriter") //
                .resource(new FileSystemResource("foos.txt"))//
                .lineAggregator(new PassThroughLineAggregator<>()) //
                .build(); //
    }

    @Bean
    public FlatFileItemWriter<Person> barItemWriter() {
        return new FlatFileItemWriterBuilder<Person>().name("barItemWriter") //
                .resource(new FileSystemResource("bars.txt")) //
                .lineAggregator(new PassThroughLineAggregator<>()) //
                .build();
    }

    @Bean
    public Step dataExtractionStep() {
        return stepBuilderFactory.get("dataExtractionStep").<Person, Person> chunk(2) //
                .reader(itemReader())//
                .writer(classifierCompositeItemWriter(fooItemWriter(), barItemWriter()))//
                .stream(fooItemWriter())//
                .stream(barItemWriter()) //
                .build();
    }

    @Bean
    public Job dataExtractionJob() {
        return jobBuilderFactory.get("dataExtractionJob").start(dataExtractionStep()).build();
    }

    public static void main(String[] args) throws Exception {
        ApplicationContext context = new AnnotationConfigApplicationContext(ClassifiierFileBatchConguration.class);
        JobLauncher jobLauncher = context.getBean(JobLauncher.class);
        Job job = context.getBean(Job.class);
        jobLauncher.run(job, new JobParameters());
    }

    public static class Person {

        private int id;

        private String name;

        public Person() {
        }

        public Person(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "Person{" + "id=" + id + ", name='" + name + '\'' + '}';
        }
    }

}
