## 프로젝트 구조 설명
  TosStreamBase    : 공통 모듈  
    com.sktelecom.tos.stream.base                      :  
                                 .data                 :  
                                   StreamData          :  
                                 .kafka                :  
                                   StreamKafkaConsumer :  
                                   StreamKafkaProducer :  
                                 .meta                 :  
                                   .info               :  
                                     FilterInfo        :  
                                     MetaInfo          :  
                                     OutputInfo        :  
                                   CEPFormatter        :  
                                   EventMapper         :  
                                   MetaInfoLoader      :  
                                 .spark                :  
                                   TosStreamApp        :  
                                 ConfigManager         :  
                                 Utils                 :  

## Process
 1. TosStreamBase 를 상속 및 사용하여 TosStreamApp 개발  
 2. ...  