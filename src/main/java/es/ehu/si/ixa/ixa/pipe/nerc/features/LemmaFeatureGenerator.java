/*
 * Copyright 2015 Rodrigo Agerri

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package es.ehu.si.ixa.ixa.pipe.nerc.features;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import opennlp.tools.util.InvalidFormatException;
import opennlp.tools.util.featuregen.ArtifactToSerializerMapper;
import opennlp.tools.util.featuregen.CustomFeatureGenerator;
import opennlp.tools.util.featuregen.FeatureGeneratorResourceProvider;
import opennlp.tools.util.model.ArtifactSerializer;
import es.ehu.si.ixa.ixa.pipe.nerc.dict.LemmaResource;
import es.ehu.si.ixa.ixa.pipe.nerc.dict.POSModelResource;

/**
 * Generate lemma as feature of current token. This feature generator can
 * also be placed in a sliding window.
 * @author ragerri
 * @version 2015-03-11
 */
public class LemmaFeatureGenerator extends CustomFeatureGenerator implements ArtifactToSerializerMapper {
  
  private POSModelResource posModelResource;
  private LemmaResource lemmaDictResource;
  private String[] currentSentence;
  private String[] currentTags;
  private Map<String, String> rangeMap;
  
  public LemmaFeatureGenerator() {
  }
  
  public void createFeatures(List<String> features, String[] tokens, int index,
      String[] previousOutcomes) {
    
    String featuresRange = rangeMap.get("range");
    //cache pos tagger results for each sentence
    if (currentSentence != tokens) {
      currentSentence = tokens;
      currentTags = posModelResource.posTag(tokens);
    }
    String posTag = currentTags[index];
    String lemma = lemmaDictResource.lookUpLemma(tokens[index], posTag);
    features.add("lemma=" + lemma);
  }
  
  @Override
  public void updateAdaptiveData(String[] tokens, String[] outcomes) {
    
  }

  @Override
  public void clearAdaptiveData() {
    
  }

  @Override
  public void init(Map<String, String> properties,
      FeatureGeneratorResourceProvider resourceProvider)
      throws InvalidFormatException {
    Object posResource = resourceProvider.getResource(properties.get("model"));
    if (!(posResource instanceof POSModelResource)) {
      throw new InvalidFormatException("Not a POSModelResource for key: " + properties.get("model"));
    }
    this.posModelResource = (POSModelResource) posResource;
    Object lemmaResource = resourceProvider.getResource(properties.get("dict"));
    if (!(lemmaResource instanceof LemmaResource)) {
      throw new InvalidFormatException("Not a LemmaResource for key: " + properties.get("dict"));
    }
    this.lemmaDictResource = (LemmaResource) lemmaResource;
    this.rangeMap = properties;
  }
  
  @Override
  public Map<String, ArtifactSerializer<?>> getArtifactSerializerMapping() {
    Map<String, ArtifactSerializer<?>> mapping = new HashMap<>();
    mapping.put("posmodelserializer", new POSModelResource.POSModelResourceSerializer());
    mapping.put("lemmadictserializer", new LemmaResource.LemmaResourceSerializer());
    return Collections.unmodifiableMap(mapping);
  }
}

